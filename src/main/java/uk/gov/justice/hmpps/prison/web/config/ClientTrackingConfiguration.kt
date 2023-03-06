package uk.gov.justice.hmpps.prison.web.config

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.text.ParseException

@Configuration
class ClientTrackingConfiguration(private val clientTrackingInterceptor: ClientTrackingInterceptor) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    log.info("Adding application insights client tracking interceptor")
    registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**")
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

@Configuration
class ClientTrackingInterceptor : HandlerInterceptor {
  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    val (user, clientId) = findUserAndClient(request)
    user?.let {
      Span.current().setAttribute("username", it) // username in customDimensions
      Span.current().setAttribute("enduser.id", it) // user_Id at the top level of the request
    }
    clientId?.let { Span.current().setAttribute("clientId", clientId) }
    return true
  }

  private fun findUserAndClient(req: HttpServletRequest): Pair<String?, String?> =
    req.getHeader(HttpHeaders.AUTHORIZATION)
      ?.takeIf { it.startsWith("Bearer ") }
      ?.let { getClaimsFromJWT(it) }
      ?.let { it.getClaim("user_name") as String? to it.getClaim("client_id") as String? }
      ?: (null to null)

  private fun getClaimsFromJWT(token: String): JWTClaimsSet? =
    try {
      SignedJWT.parse(token.replace("Bearer ", ""))
    } catch (e: ParseException) {
      log.warn("problem decoding jwt public key for application insights", e)
      null
    }?.jwtClaimsSet

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
