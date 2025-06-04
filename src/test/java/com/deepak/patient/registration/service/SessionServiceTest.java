package com.deepak.patient.registration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    private SessionService sessionService;
    private final long shortExpiryTimeMs = 50; // For quick expiration testing
    private final long longExpiryTimeMs = 100000; // For non-expiration testing
    private final String defaultRefreshTokenKey = "refreshTokenExpirationMs";
    private final String defaultSessionDataKey = "sessionDataTimeoutMs";


    @BeforeEach
    void setUp() {
        sessionService = new SessionService();
        // Set longer default expiry for most tests to avoid premature expiration
        ReflectionTestUtils.setField(sessionService, defaultRefreshTokenKey, longExpiryTimeMs);
        ReflectionTestUtils.setField(sessionService, defaultSessionDataKey, longExpiryTimeMs);
    }

    // --- Refresh Token Blacklist Tests ---

    @Test
    void blacklistRefreshToken_shouldAddTokenToBlacklist() {
        String token = "testToken1";
        sessionService.blacklistRefreshToken(token);
        assertTrue(sessionService.isRefreshTokenBlacklisted(token));
    }

    @Test
    void isRefreshTokenBlacklisted_shouldReturnFalse_forNonBlacklistedToken() {
        assertFalse(sessionService.isRefreshTokenBlacklisted("nonExistentToken"));
    }

    @Test
    void isRefreshTokenBlacklisted_shouldReturnTrue_forActiveBlacklistedToken() {
        String token = "testTokenActive";
        sessionService.blacklistRefreshToken(token); // Uses longExpiryTimeMs by default from setUp
        assertTrue(sessionService.isRefreshTokenBlacklisted(token));
    }

    @Test
    void isRefreshTokenBlacklisted_shouldReturnFalseAndRemove_forExpiredBlacklistedToken() throws InterruptedException {
        ReflectionTestUtils.setField(sessionService, defaultRefreshTokenKey, shortExpiryTimeMs);
        String token = "testTokenExpired";
        sessionService.blacklistRefreshToken(token);

        assertTrue(sessionService.isRefreshTokenBlacklisted(token), "Token should be blacklisted initially");

        Thread.sleep(shortExpiryTimeMs + 30); // Wait a bit longer than expiry

        assertFalse(sessionService.isRefreshTokenBlacklisted(token), "Token should no longer be blacklisted after expiry");

        ConcurrentHashMap<String, Long> blacklist = getInternalBlacklistMap(sessionService);
        assertFalse(blacklist.containsKey(token), "Expired token should be removed from the map");
    }

    @Test
    void blacklistRefreshToken_shouldCleanupOtherExpiredTokensWhenAddingNew() throws InterruptedException {
        ReflectionTestUtils.setField(sessionService, defaultRefreshTokenKey, shortExpiryTimeMs);
        String tokenToExpire = "expiredToken1";
        String tokenToStayShort = "activeShortLivedToken";

        sessionService.blacklistRefreshToken(tokenToExpire); // Will expire soon

        Thread.sleep(shortExpiryTimeMs + 30); // Ensure tokenToExpire is definitely expired

        // Adding a new token should trigger cleanup of tokenToExpire
        sessionService.blacklistRefreshToken(tokenToStayShort);

        ConcurrentHashMap<String, Long> blacklist = getInternalBlacklistMap(sessionService);
        assertFalse(blacklist.containsKey(tokenToExpire), "Expired token1 should have been cleaned up");
        assertTrue(blacklist.containsKey(tokenToStayShort), "Newly added token should exist");
    }

    // --- Session Data Management Tests ---

    @Test
    void storeSessionData_shouldStoreDataWithDefaultTimeout() {
        String userId = "user1";
        String data = "sessionData";
        // Using the storeSessionData version that relies on default sessionDataTimeoutMs
        sessionService.storeSessionData(userId, data);
        assertEquals(data, sessionService.getSessionData(userId));
    }

    @Test
    void storeSessionData_shouldStoreDataWithSpecificTimeout() {
        String userId = "user1Specific";
        String data = "sessionDataSpecific";
        sessionService.storeSessionData(userId, data, longExpiryTimeMs); // Explicit long timeout
        assertEquals(data, sessionService.getSessionData(userId));
    }


    @Test
    void getSessionData_shouldReturnNull_forNonExistentUser() {
        assertNull(sessionService.getSessionData("nonExistentUser"));
    }

    @Test
    void getSessionData_shouldReturnNullAndRemove_forExpiredSessionData() throws InterruptedException {
        ReflectionTestUtils.setField(sessionService, defaultSessionDataKey, shortExpiryTimeMs);
        String userId = "userExpiredSession";
        String data = "expiredData";
        sessionService.storeSessionData(userId, data); // Uses default shortExpiryTimeMs for session data

        assertTrue(sessionService.getSessionData(userId) != null, "Session data should exist initially");

        Thread.sleep(shortExpiryTimeMs + 30);

        assertNull(sessionService.getSessionData(userId), "Session data should be null after expiry");
        Map<String, ?> sessionMap = getInternalSessionMap(sessionService);
        assertFalse(sessionMap.containsKey(userId), "Expired session data should be removed from the map");
    }

    @Test
    void storeSessionData_shouldCleanupOtherExpiredSessionsWhenAddingNew() throws InterruptedException {
        ReflectionTestUtils.setField(sessionService, defaultSessionDataKey, shortExpiryTimeMs);
        String userToExpire = "userSessionToExpire";
        String userToStay = "userSessionToStay";

        sessionService.storeSessionData(userToExpire, "dataToExpire"); // Will expire soon due to short default

        Thread.sleep(shortExpiryTimeMs + 30); // Ensure userToExpire data is expired

        // Store another session (with long timeout by default from setUp, or explicitly if needed)
        // This action should trigger cleanup of userToExpire's session
        // For clarity, let's re-assert the long timeout for the new session or use the specific timeout variant
        ReflectionTestUtils.setField(sessionService, defaultSessionDataKey, longExpiryTimeMs);
        sessionService.storeSessionData(userToStay, "dataToStay");

        Map<String, ?> sessionMap = getInternalSessionMap(sessionService);
        assertFalse(sessionMap.containsKey(userToExpire), "Expired session for userToExpire should be cleaned up.");
        assertTrue(sessionMap.containsKey(userToStay), "Session for userToStay should exist.");
    }


    @Test
    void invalidateSession_shouldRemoveSessionData() {
        String userId = "userToInvalidate";
        String data = "someData";
        sessionService.storeSessionData(userId, data, longExpiryTimeMs);
        assertNotNull(sessionService.getSessionData(userId), "Session data should be present before invalidation");

        sessionService.invalidateSession(userId);
        assertNull(sessionService.getSessionData(userId), "Session data should be null after invalidation");
        Map<String, ?> sessionMap = getInternalSessionMap(sessionService);
        assertFalse(sessionMap.containsKey(userId), "Session data should be removed from the map after invalidation");
    }

    @Test
    void invalidateSession_shouldDoNothing_forNonExistentUser() {
        assertDoesNotThrow(() -> sessionService.invalidateSession("nonExistentUserForInvalidate"));
        Map<String, ?> sessionMap = getInternalSessionMap(sessionService);
        assertTrue(sessionMap.isEmpty(), "Session map should remain empty or unchanged");
    }


    // Helper to access internal blacklist map via reflection for verification
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, Long> getInternalBlacklistMap(SessionService service) {
        return (ConcurrentHashMap<String, Long>) ReflectionTestUtils.getField(service, "refreshTokenBlacklist");
    }

    // Helper to access internal session map via reflection for verification
    // The SessionService stores SessionEntry objects in sessionMap
    @SuppressWarnings("unchecked")
    private Map<String, Object> getInternalSessionMap(SessionService service) {
        // Assuming sessionMap stores Object or a specific SessionEntry type
        return (Map<String, Object>) ReflectionTestUtils.getField(service, "sessionMap");
    }
}
