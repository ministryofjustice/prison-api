package uk.gov.justice.hmpps.prison.web.config

import com.microsoft.applicationinsights.TelemetryClient
import io.micrometer.azuremonitor.AzureMonitorConfig
import io.micrometer.azuremonitor.AzureMonitorMeterRegistry
import io.micrometer.core.instrument.Clock.SYSTEM
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * TelemetryClient gets altered at runtime by the java agent and so is a no-op otherwise
 */
@Configuration
class ApplicationInsightsConfiguration {
  @Bean
  fun telemetryClient(): TelemetryClient = TelemetryClient()

  @Bean
  @ConditionalOnProperty("applicationinsights.connection.string")
  fun azureMonitorMeterRegistry(@Value($$"${applicationinsights.connection.string}") connectionString: String): AzureMonitorMeterRegistry = AzureMonitorMeterRegistry(
    object : AzureMonitorConfig {
      override fun get(key: String): String? = null
      override fun connectionString(): String = connectionString
    },
    SYSTEM,
  )
}

fun TelemetryClient.trackEvent(name: String, properties: Map<String, String>) = this.trackEvent(name, properties, null)
