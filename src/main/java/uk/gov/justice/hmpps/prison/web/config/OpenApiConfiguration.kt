package uk.gov.justice.hmpps.prison.web.config

import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
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
        Server().url("https://prison-api-dev.prison.service.justice.gov.uk").description("Development"),
        Server().url("https://prison-api-preprod.prison.service.justice.gov.uk").description("PreProd"),
        Server().url("https://prison-api.prison.service.justice.gov.uk").description("Prod"),
        Server().url("http://localhost:8080").description("Local"),
      ),
    )
    .components(
      Components().addSecuritySchemes(
        "bearer-jwt",
        SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .`in`(SecurityScheme.In.HEADER)
          .name("Authorization"),
      ),
    )
    .info(
      Info().title("HMPPS Prison API Documentation")
        .version(version)
        .description(this.javaClass.getResource("/documentation/service-description.html")!!.readText())
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk"))
        .license(License().name("MIT").url("https://opensource.org/licenses/MIT")),
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))

  @Bean
  fun openAPICustomiser(): OpenApiCustomizer = OpenApiCustomizer { }.also {
    PrimitiveType.enablePartialTime() // Prevents generation of a LocalTime schema which causes conflicts with java.time.LocalTime
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
