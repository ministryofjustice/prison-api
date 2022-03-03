package uk.gov.justice.hmpps.prison.web.config

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.util.ReflectionUtils
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration
import springfox.boot.starter.autoconfigure.SpringfoxConfigurationProperties
import springfox.boot.starter.autoconfigure.SwaggerUiWebFluxConfiguration
import springfox.boot.starter.autoconfigure.SwaggerUiWebMvcConfiguration
import springfox.documentation.oas.configuration.OpenApiMappingConfiguration
import springfox.documentation.oas.configuration.OpenApiWebFluxConfiguration
import springfox.documentation.oas.configuration.OpenApiWebMvcConfiguration
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration
import springfox.documentation.spring.web.SpringfoxWebConfiguration
import springfox.documentation.spring.web.SpringfoxWebFluxConfiguration
import springfox.documentation.spring.web.SpringfoxWebMvcConfiguration
import springfox.documentation.spring.web.json.JacksonModuleRegistrar
import springfox.documentation.spring.web.json.JsonSerializer
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider
import springfox.documentation.swagger.configuration.SwaggerCommonConfiguration
import java.lang.reflect.Field

// Copied from springfox.boot.starter.autoconfigure.OpenApiAutoConfiguration
// so can exclude SpringfoxWebConfiguration and thus have our own override of jsonSerializer
@Configuration
@EnableConfigurationProperties(SpringfoxConfigurationProperties::class)
@Import(
  SpringfoxWebMvcConfiguration::class,
  SpringfoxWebFluxConfiguration::class,
  SwaggerCommonConfiguration::class,
  OpenApiMappingConfiguration::class,
  OpenApiWebMvcConfiguration::class,
  OpenApiWebFluxConfiguration::class,
  SpringDataRestConfiguration::class,
  BeanValidatorPluginsConfiguration::class,
  SwaggerUiWebFluxConfiguration::class,
  SwaggerUiWebMvcConfiguration::class
)
@AutoConfigureAfter(
  WebMvcAutoConfiguration::class,
  JacksonAutoConfiguration::class,
  HttpMessageConvertersAutoConfiguration::class,
  RepositoryRestMvcAutoConfiguration::class
)
@ComponentScan(basePackages = ["springfox.documentation.oas.web", "springfox.documentation.oas.mappers"])
class OpenApiDocumentationConfiguration

@Configuration
class SpringFoxConfiguration : SpringfoxWebConfiguration() {
  @Bean
  fun springfoxHandlerProviderBeanPostProcessor(): BeanPostProcessor = object : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
      if (bean is WebMvcRequestHandlerProvider || bean is WebFluxRequestHandlerProvider) {
        customizeSpringfoxHandlerMappings(getHandlerMappings(bean))
      }
      return bean
    }
  }

  override fun jsonSerializer(moduleRegistrars: MutableList<JacksonModuleRegistrar>?): JsonSerializer =
    SwaggerJsonSerializer()

  private fun <T : RequestMappingInfoHandlerMapping?> customizeSpringfoxHandlerMappings(mappings: MutableList<T>) {
    val copy = mappings.stream()
      .filter { mapping: T -> mapping?.patternParser == null }
      .toList()
    mappings.clear()
    mappings.addAll(copy)
  }

  private fun getHandlerMappings(bean: Any): MutableList<RequestMappingInfoHandlerMapping> {
    val field: Field? = ReflectionUtils.findField(bean.javaClass, "handlerMappings")
    field?.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return field?.get(bean) as MutableList<RequestMappingInfoHandlerMapping>
  }
}
