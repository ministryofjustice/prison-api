package uk.gov.justice.hmpps.prison.web.config

import com.fasterxml.jackson.core.JsonProcessingException
import com.google.common.collect.Lists
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.Contact
import springfox.documentation.service.SecurityReference
import springfox.documentation.service.SecurityScheme
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.json.Json
import springfox.documentation.spring.web.json.JsonSerializer
import springfox.documentation.spring.web.plugins.Docket
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Optional

@Configuration
class SwaggerConfig(buildProperties: BuildProperties) {
  private val version = buildProperties.version

  internal enum class PassAs {
    header, cookie
  }

  @Bean
  fun docket(): Docket {
    val apiKey = ApiKey(SECURITY_SCHEME_REF, AUTHORIZATION_HEADER, PassAs.header.name)
    return Docket(DocumentationType.OAS_30)
      .select()
      .apis(RequestHandlerSelectors.withClassAnnotation(RestController::class.java))
      .paths(PathSelectors.any())
      .build()
      .apiInfo(apiInfo())
      .genericModelSubstitutes(Optional::class.java)
      .directModelSubstitute(ZonedDateTime::class.java, java.util.Date::class.java)
      .directModelSubstitute(LocalDateTime::class.java, java.util.Date::class.java)
      .directModelSubstitute(LocalDate::class.java, Date::class.java)
      .securityContexts(Lists.newArrayList(securityContext()))
      .securitySchemes(Lists.newArrayList<SecurityScheme>(apiKey))
      .forCodeGeneration(true)
  }

  private fun securityContext(): SecurityContext = SecurityContext.builder()
    .securityReferences(listOf(SecurityReference(SECURITY_SCHEME_REF, arrayOfNulls(0))))
    .forPaths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN))
    .build()

  private fun apiInfo(): ApiInfo = ApiInfo(
    "HMPPS Prison API Documentation",
    "A RESTful API service for accessing NOMIS data sets.\n\nAll times sent to the API should be sent in local time without the timezone e.g. YYYY-MM-DDTHH:MM:SS.  All times returned in responses will be in Europe / London local time unless otherwise stated.",
    version,
    "https://sign-in.hmpps.service.justice.gov.uk/auth/terms",
    contactInfo(),
    "Open Government Licence v3.0",
    "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/",
    emptyList(),
  )

  private fun contactInfo(): Contact = Contact("HMPPS Digital Studio", null, "feedback@digital.justice.gov.uk")

  companion object {
    const val DEFAULT_INCLUDE_PATTERN = "/api/.*"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val SECURITY_SCHEME_REF = "Authorization"
  }
}

open class SwaggerJsonSerializer : JsonSerializer(listOf()) {
  private val objectMapper = io.swagger.v3.core.util.Json.mapper()

  override fun toJson(toSerialize: Any?): Json = try {
    Json(objectMapper.writeValueAsString(toSerialize))
  } catch (e: JsonProcessingException) {
    throw RuntimeException("Could not write JSON", e)
  }
}
