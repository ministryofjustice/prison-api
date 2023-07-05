package uk.gov.justice.hmpps.prison.util.builders

import org.springframework.http.HttpHeaders
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper
import uk.gov.justice.hmpps.prison.util.JwtParameters
import java.time.Duration
import java.util.function.Consumer

abstract class WebClientEntityBuilder {
  protected fun setAuthorisation(
    jwtAuthenticationHelper: JwtAuthenticationHelper,
    roles: List<String>,
  ): Consumer<HttpHeaders> = Consumer { httpHeaders: HttpHeaders ->
    httpHeaders.add(
      "Authorization",
      "Bearer " + validToken(jwtAuthenticationHelper, roles),
    )
  }

  protected fun validToken(jwtAuthenticationHelper: JwtAuthenticationHelper, roles: List<String>): String =
    jwtAuthenticationHelper.createJwt(
      JwtParameters.builder()
        .username("ITAG_USER")
        .scope(java.util.List.of("read", "write"))
        .roles(roles)
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
}
