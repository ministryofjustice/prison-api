package uk.gov.justice.hmpps.prison.util.builders

import org.springframework.http.HttpHeaders
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.util.function.Consumer

abstract class WebClientEntityBuilder {
  protected fun setAuthorisation(
    jwtAuthenticationHelper: JwtAuthorisationHelper,
    roles: List<String>,
  ): Consumer<HttpHeaders> = Consumer { httpHeaders: HttpHeaders ->
    httpHeaders.add(
      "Authorization",
      "Bearer " + validToken(jwtAuthenticationHelper, roles),
    )
  }

  protected fun validToken(jwtAuthenticationHelper: JwtAuthorisationHelper, roles: List<String>): String =
    jwtAuthenticationHelper.createJwtAccessToken(
      username = "ITAG_USER",
      scope = listOf("read", "write"),
      roles = roles,
    )
}
