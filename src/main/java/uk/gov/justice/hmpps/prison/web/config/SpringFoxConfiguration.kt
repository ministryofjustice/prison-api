package uk.gov.justice.hmpps.prison.web.config

import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ReflectionUtils
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider
import java.lang.reflect.Field

@Configuration
open class SpringFoxConfiguration {
  @Bean
  open fun springfoxHandlerProviderBeanPostProcessor(): BeanPostProcessor = object : BeanPostProcessor {
    @Throws(BeansException::class)
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
      if (bean is WebMvcRequestHandlerProvider || bean is WebFluxRequestHandlerProvider) {
        customizeSpringfoxHandlerMappings(getHandlerMappings(bean))
      }
      return bean
    }

    private fun <T : RequestMappingInfoHandlerMapping?> customizeSpringfoxHandlerMappings(mappings: MutableList<T>) {
      val copy = mappings.stream()
        .filter { mapping: T -> mapping?.patternParser == null }
        .toList()
      mappings.clear()
      mappings.addAll(copy)
    }

    private fun getHandlerMappings(bean: Any): MutableList<RequestMappingInfoHandlerMapping> = try {
      val field: Field? = ReflectionUtils.findField(bean.javaClass, "handlerMappings")
      field?.isAccessible = true
      @Suppress("UNCHECKED_CAST")
      field?.get(bean) as MutableList<RequestMappingInfoHandlerMapping>
    } catch (e: IllegalArgumentException) {
      throw IllegalStateException(e)
    } catch (e: IllegalAccessException) {
      throw IllegalStateException(e)
    }
  }
}
