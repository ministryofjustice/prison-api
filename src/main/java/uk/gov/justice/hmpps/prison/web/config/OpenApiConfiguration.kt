package uk.gov.justice.hmpps.prison.web.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://api.prison.service.justice.gov.uk").description("Prod"),
        Server().url("https://api-preprod.prison.service.justice.gov.uk").description("PreProd"),
        Server().url("https://api-dev.prison.service.justice.gov.uk").description("Development"),
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
            """
        )
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk"))
        .license(License().name("MIT").url("https://opensource.org/licenses/MIT"))
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))
}
