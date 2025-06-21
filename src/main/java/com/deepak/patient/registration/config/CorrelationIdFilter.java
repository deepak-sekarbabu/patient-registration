package com.deepak.patient.registration.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

/**
 * Filter that handles correlation IDs and request/response logging.
 *
 * <p>This filter will: 1. Extract or generate a correlation ID for each request 2. Add the
 * correlation ID to the MDC for logging 3. Log request/response details 4. Add correlation ID to
 * the response headers
 */
@Component
@Order(1) // High priority to ensure it runs early
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
  public static final String CORRELATION_ID_MDC_KEY = "correlationId";
  public static final String TRACE_ID_MDC_KEY = "traceId";
  public static final String SPAN_ID_MDC_KEY = "spanId";

  private static final List<String> HEADERS_TO_LOG =
      List.of("Authorization", "Content-Type", "Accept", "User-Agent");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Wrap request/response to allow multiple reads of the body
    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

    try {
      // Get or generate correlation ID
      String correlationId = getCorrelationId(request);

      // Add correlation ID to MDC and response headers
      MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
      response.setHeader(CORRELATION_ID_HEADER, correlationId);

      // Log request details
      logRequest(wrappedRequest, correlationId);

      // Process the request
      filterChain.doFilter(wrappedRequest, wrappedResponse);

    } finally {
      // Log response details
      logResponse(wrappedResponse, MDC.get(CORRELATION_ID_MDC_KEY));

      // Copy body from wrapped response to actual response
      wrappedResponse.copyBodyToResponse();

      // Clear MDC
      MDC.clear();
    }
  }

  private String getCorrelationId(HttpServletRequest request) {
    // Try to get correlation ID from header, or generate a new one
    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = "gen-" + UUID.randomUUID().toString();
    }
    return correlationId;
  }

  private void logRequest(ContentCachingRequestWrapper request, String correlationId) {
    StringBuilder msg = new StringBuilder();
    msg.append("Incoming Request: ")
        .append(request.getMethod())
        .append(" ")
        .append(request.getRequestURI());

    // Add query string if present
    if (request.getQueryString() != null) {
      msg.append("?").append(request.getQueryString());
    }

    // Add headers
    msg.append(" [Headers: ");
    for (String header : HEADERS_TO_LOG) {
      String value = request.getHeader(header);
      if (value != null) {
        msg.append(header).append("=").append(redactSensitive(header, value)).append(" ");
      }
    }
    msg.append("]");

    // Add request body if present
    String requestBody = getRequestBody(request);
    if (requestBody != null && !requestBody.isBlank()) {
      msg.append(" [Body: ").append(abbreviate(requestBody, 1000)).append("]");
    }

    logger.info(msg.toString());
  }

  private void logResponse(ContentCachingResponseWrapper wrappedResponse, String correlationId) {
    int status = wrappedResponse.getStatus();
    String msg =
        String.format(
            "Outgoing Response: %d [Correlation-ID: %s] [Content-Type: %s] [Body: %s]",
            status,
            correlationId,
            wrappedResponse.getContentType(),
            getResponsePayload(wrappedResponse));

    if (status >= 400) {
      logger.error(msg);
    } else if (status >= 300) {
      logger.warn(msg);
    } else {
      logger.info(msg);
    }
  }

  private String getRequestBody(ContentCachingRequestWrapper request) {
    ContentCachingRequestWrapper wrapper =
        WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
    if (wrapper != null) {
      byte[] buf = wrapper.getContentAsByteArray();
      if (buf.length > 0) {
        try {
          return new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
        } catch (UnsupportedEncodingException ex) {
          return "[unknown]" + ex.getMessage();
        }
      }
    }
    return "";
  }

  private String getResponsePayload(HttpServletResponse response) {
    ContentCachingResponseWrapper wrapper =
        WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
    if (wrapper != null) {
      byte[] buf = wrapper.getContentAsByteArray();
      if (buf.length > 0) {
        try {
          return abbreviate(new String(buf, 0, buf.length, wrapper.getCharacterEncoding()), 1000);
        } catch (UnsupportedEncodingException ex) {
          return "[unknown]" + ex.getMessage();
        }
      }
    }
    return "";
  }

  private String redactSensitive(String header, String value) {
    if ("Authorization".equalsIgnoreCase(header)) {
      return "[REDACTED]";
    }
    return value;
  }

  private String abbreviate(String str, int maxWidth) {
    if (str == null || str.length() <= maxWidth) {
      return str;
    }
    return str.substring(0, maxWidth) + "... [truncated]";
  }
}
