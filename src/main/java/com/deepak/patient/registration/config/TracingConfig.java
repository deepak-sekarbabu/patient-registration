package com.deepak.patient.registration.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenTelemetry tracing. Configures tracing with OTLP gRPC exporter and W3C trace
 * context propagation.
 */
@Configuration(proxyBeanMethods = false)
public class TracingConfig {

  @Value("${spring.application.name:patient-registration}")
  private String applicationName;

  @Value("${management.otlp.traces.endpoint:http://localhost:4317}")
  private String otlpEndpoint;

  /** Configures and provides an OpenTelemetry instance with OTLP exporter. */
  @Bean
  public OpenTelemetry openTelemetry() {
    // Define resource attributes
    Resource resource =
        Resource.getDefault()
            .merge(
                Resource.create(
                    Attributes.of(
                        ResourceAttributes.SERVICE_NAME,
                        applicationName,
                        ResourceAttributes.DEPLOYMENT_ENVIRONMENT,
                        "production")));

    // Configure span exporter
    OtlpGrpcSpanExporter spanExporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint(otlpEndpoint)
            .setTimeout(2, TimeUnit.SECONDS)
            .build();

    // Configure span processor
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
            .setResource(resource)
            .build();

    // Build and return OpenTelemetry instance
    return OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .buildAndRegisterGlobal();
  }

  /** Provides an OpenTelemetry Tracer instance. */
  @Bean
  public Tracer tracer(OpenTelemetry openTelemetry) {
    return openTelemetry.getTracer(applicationName);
  }
}
