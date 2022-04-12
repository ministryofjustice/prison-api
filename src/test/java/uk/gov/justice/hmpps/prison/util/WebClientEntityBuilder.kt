package uk.gov.justice.hmpps.prison.util

import org.springframework.http.HttpHeaders
import java.time.Duration
import java.util.function.Consumer

abstract class WebClientEntityBuilder(private val jwtAuthenticationHelper: JwtAuthenticationHelper) {
  protected fun setAuthorisation(roles: List<String>): Consumer<HttpHeaders> {
    return Consumer { httpHeaders: HttpHeaders ->
      httpHeaders.add(
        "Authorization",
        "Bearer " + validToken(roles)
      )
    }
  }

  protected fun validToken(roles: List<String?>?): String? {
    return jwtAuthenticationHelper.createJwt(
      JwtParameters.builder()
        .username("ITAG_USER")
        .scope(java.util.List.of("read", "write"))
        .roles(roles)
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build()
    )
  }
}
