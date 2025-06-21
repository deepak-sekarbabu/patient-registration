package com.deepak.patient.registration.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that handles GZIP compressed request bodies. Automatically decompresses requests with
 * 'Content-Encoding: gzip' header.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GzipRequestFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      @SuppressWarnings("null") HttpServletRequest request, @SuppressWarnings("null") HttpServletResponse response, @SuppressWarnings("null") FilterChain filterChain)
      throws ServletException, IOException {

    String contentEncoding = request.getHeader(HttpHeaders.CONTENT_ENCODING);

    if (contentEncoding != null && contentEncoding.contains("gzip")) {
      request = new GzipServletRequestWrapper(request);
    }

    filterChain.doFilter(request, response);
  }

  /** Wrapper class that decompresses GZIP content on-the-fly. */
  private static class GzipServletRequestWrapper
      extends jakarta.servlet.http.HttpServletRequestWrapper {
    private final byte[] decompressedBody;

    public GzipServletRequestWrapper(HttpServletRequest request) throws IOException {
      super(request);
      try (GZIPInputStream gzipInputStream = new GZIPInputStream(request.getInputStream())) {
        this.decompressedBody = gzipInputStream.readAllBytes();
      }
    }

    @Override
    public ServletInputStream getInputStream() {
      return new jakarta.servlet.ServletInputStream() {
        private final java.io.ByteArrayInputStream inputStream =
            new java.io.ByteArrayInputStream(decompressedBody);

        @Override
        public int read() {
          return inputStream.read();
        }

        @Override
        public boolean isFinished() {
          return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
          return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
          throw new UnsupportedOperationException("Not implemented");
        }
      };
    }
  }
}
