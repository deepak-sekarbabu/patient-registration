package com.deepak.patient.registration.util;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Utility class for working with correlation IDs in a distributed system.
 *
 * <p>This class provides thread-safe access to the current correlation ID that's stored in the MDC
 * (Mapped Diagnostic Context).
 */
@Component
public class CorrelationIdUtils {

  public static final String CORRELATION_ID_MDC_KEY = "correlationId";
  public static final String TRACE_ID_MDC_KEY = "traceId";
  public static final String SPAN_ID_MDC_KEY = "spanId";

  private CorrelationIdUtils() {
    // Private constructor to prevent instantiation
  }

  /**
   * Get the current correlation ID from the MDC.
   *
   * @return the current correlation ID, or null if not set
   */
  public static String getCorrelationId() {
    return MDC.get(CORRELATION_ID_MDC_KEY);
  }

  /**
   * Get the current trace ID from the MDC.
   *
   * @return the current trace ID, or null if not set
   */
  public static String getTraceId() {
    return MDC.get(TRACE_ID_MDC_KEY);
  }

  /**
   * Get the current span ID from the MDC.
   *
   * @return the current span ID, or null if not set
   */
  public static String getSpanId() {
    return MDC.get(SPAN_ID_MDC_KEY);
  }

  /**
   * Get a string representation of the current tracing context.
   *
   * @return a string in the format "[traceId,spanId,correlationId]"
   */
  public static String getTracingContext() {
    return String.format(
        "[%s,%s,%s]",
        getTraceId() != null ? getTraceId() : "-",
        getSpanId() != null ? getSpanId() : "-",
        getCorrelationId() != null ? getCorrelationId() : "-");
  }

  /**
   * Set the correlation ID in the MDC.
   *
   * @param correlationId the correlation ID to set
   */
  public static void setCorrelationId(String correlationId) {
    if (correlationId != null) {
      MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
    }
  }

  /**
   * Set the trace ID in the MDC.
   *
   * @param traceId the trace ID to set
   */
  public static void setTraceId(String traceId) {
    if (traceId != null) {
      MDC.put(TRACE_ID_MDC_KEY, traceId);
    }
  }

  /**
   * Set the span ID in the MDC.
   *
   * @param spanId the span ID to set
   */
  public static void setSpanId(String spanId) {
    if (spanId != null) {
      MDC.put(SPAN_ID_MDC_KEY, spanId);
    }
  }

  /** Clear all tracing information from the MDC. */
  public static void clear() {
    MDC.remove(CORRELATION_ID_MDC_KEY);
    MDC.remove(TRACE_ID_MDC_KEY);
    MDC.remove(SPAN_ID_MDC_KEY);
  }
}
