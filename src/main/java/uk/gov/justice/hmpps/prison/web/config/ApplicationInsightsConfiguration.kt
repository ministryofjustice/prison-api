package uk.gov.justice.hmpps.prison.web.config

import com.microsoft.applicationinsights.TelemetryClient
import io.micrometer.azuremonitor.AzureMonitorConfig
import io.micrometer.azuremonitor.AzureMonitorMeterRegistry
import io.micrometer.core.instrument.Clock.SYSTEM
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * TelemetryClient gets altered at runtime by the java agent and so is a no-op otherwise
 */
@Configuration
class ApplicationInsightsConfiguration(@Autowired private val applicationContext: ApplicationContext) {
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

  @EventListener(ApplicationReadyEvent::class)
  fun printAllEndpoints() {
    val mappings = applicationContext.getBeansOfType(RequestMappingHandlerMapping::class.java)
    mappings.forEach { it.value.handlerMethods.forEach({ (k, _) -> println(k) }) }
  }
}

fun TelemetryClient.trackEvent(name: String, properties: Map<String, String>) = this.trackEvent(name, properties, null)
