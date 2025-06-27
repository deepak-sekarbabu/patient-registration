package com.deepak.logging.registration.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CorrelationIdFilter implements Filter {

  private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";
  private static final String CORRELATION_ID_MDC_KEY = "traceId";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    String correlationId = httpServletRequest.getHeader(CORRELATION_ID_HEADER_NAME);

    if (!StringUtils.hasText(correlationId)) {
      correlationId = UUID.randomUUID().toString();
    }

    MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(CORRELATION_ID_MDC_KEY);
    }
  }
}
