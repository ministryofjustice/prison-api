package uk.gov.justice.hmpps.prison.web.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import uk.gov.justice.hmpps.prison.core.SlowReportQuery

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://api-dev.prison.service.justice.gov.uk").description("Development"),
        Server().url("https://api-preprod.prison.service.justice.gov.uk").description("PreProd"),
        Server().url("https://api.prison.service.justice.gov.uk").description("Prod"),
        Server().url("http://localhost:8080").description("Local"),
      )
    )
    .components(
      Components().addSecuritySchemes(
        "bearer-jwt",
        SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .`in`(SecurityScheme.In.HEADER)
          .name("Authorization")
      )
    )
    .info(
      Info().title("HMPPS Prison API Documentation")
        .version(version)
        .description(
          """
            A RESTful API service for accessing NOMIS data sets.
            
            All times sent to the API should be sent in local time without the timezone e.g. YYYY-MM-DDTHH:MM:SS.
            All times returned in responses will be in Europe / London local time unless otherwise stated.
            
            Some endpoints are described as using the Replica database, a read-only copy of the live database which at
            time of writing lags by < 1 second up to approximately 2 seconds. These endpoints are not suitable for use
            by services reacting to events or refreshing web pages where a change has just been made.
            """
        )
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk"))
        .license(License().name("MIT").url("https://opensource.org/licenses/MIT"))
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))

  @Bean
  fun openAPICustomiser(): OpenApiCustomizer = OpenApiCustomizer {
    it.components.schemas.forEach { (_, schema: Schema<*>) ->
      val properties = schema.properties ?: mutableMapOf()
      for (propertyName in properties.keys) {
        val propertySchema = properties[propertyName]!!
        if (propertySchema is DateTimeSchema) {
          properties.replace(
            propertyName,
            StringSchema()
              .example("2021-07-05T10:35:17")
              .pattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$")
              .description(propertySchema.description)
              .required(propertySchema.required)
          )
        }
      }
    }
  }
}

@Component
class SlowReportQueryCustomizer : OperationCustomizer {
  override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
    handlerMethod.getMethodAnnotation(SlowReportQuery::class.java)?.let {
      operation.description("${operation.description ?: ""}<p>This endpoint uses the REPLICA database.</p>")
    }
    return operation
  }
}
