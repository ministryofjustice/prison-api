package uk.gov.justice.hmpps.prison.web.config

import ch.qos.logback.classic.Level
import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext
import com.microsoft.applicationinsights.web.internal.ThreadContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper
import uk.gov.justice.hmpps.prison.util.JwtParameters
import uk.gov.justice.hmpps.prison.util.findLogAppender

@Import(JwtAuthenticationHelper::class, ClientTrackingInterceptor::class, ClientTrackingConfiguration::class)
@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class])
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
class ClientTrackingConfigurationTest {
  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private lateinit var clientTrackingInterceptor: ClientTrackingInterceptor

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private lateinit var jwtAuthHelper: JwtAuthenticationHelper

  private val req = MockHttpServletRequest()
  private val res = MockHttpServletResponse()

  @BeforeEach
  fun setup() {
    ThreadContext.setRequestTelemetryContext(RequestTelemetryContext(1L))
  }

  @AfterEach
  fun tearDown() {
    ThreadContext.remove()
  }

  @Test
  fun shouldAddClientIdAndUserNameToInsightTelemetry() {
    val token = jwtAuthHelper.createJwt(JwtParameters.builder().username("bob").build())
    req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    clientTrackingInterceptor.preHandle(req, res, "null")
    val insightTelemetry = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties
    assertThat(insightTelemetry).containsExactlyInAnyOrderEntriesOf(
      mapOf(
        "username" to "bob",
        "clientId" to "prison-api-client"
      )
    )
  }

  @Test
  fun shouldAddOnlyClientIdIfUsernameNullToInsightTelemetry() {
    val token = jwtAuthHelper.createJwt(JwtParameters.builder().build())
    req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    val res = MockHttpServletResponse()
    clientTrackingInterceptor.preHandle(req, res, "null")
    val insightTelemetry = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties
    assertThat(insightTelemetry).containsExactlyInAnyOrderEntriesOf(mapOf("clientId" to "prison-api-client"))
  }

  @Test
  fun `should cope with no authorisation`() {
    clientTrackingInterceptor.preHandle(req, res, "null")
    val insightTelemetry = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties
    assertThat(insightTelemetry).isEmpty()
  }

  @Test
  fun `should allow bad JwtToken and log a warning, but cannot send clientId or username to insight telemetry`() {
    val token = "This is not a valid token"
    req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    val logAppender = findLogAppender(ClientTrackingInterceptor.Companion::class.java)

    clientTrackingInterceptor.preHandle(req, res, "null")

    // The lack of an exception here shows that a bad token does not prevent Telemetry
    val insightTelemetry = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties
    assertThat(insightTelemetry).isEmpty()
    assertThat(logAppender.list).anyMatch { it.message.contains("problem decoding jwt") && it.level == Level.WARN }
  }
}
