package uk.gov.justice.hmpps.prison.api.resource.impl

import com.microsoft.applicationinsights.TelemetryClient
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.json.JsonContent
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ResolvableType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.hmpps.prison.PrisonApiServer
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper
import uk.gov.justice.hmpps.prison.util.JwtParameters.Companion.builder
import uk.gov.justice.hmpps.prison.util.builders.TestDataContext
import java.time.Duration
import java.util.Objects
import java.util.function.Consumer

@ActiveProfiles(value = ["test"])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [PrisonApiServer::class])
@AutoConfigureTestEntityManager
abstract class ResourceTest {
  @Autowired
  protected lateinit var dataLoader: DataLoaderRepository

  @Autowired
  protected lateinit var entityManager: TestEntityManager

  @Autowired
  protected lateinit var testRestTemplate: TestRestTemplate

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @SpyBean
  protected lateinit var telemetryClient: TelemetryClient

  @Autowired
  protected lateinit var jwtAuthenticationHelper: JwtAuthenticationHelper

  @Autowired
  protected lateinit var authTokenHelper: AuthTokenHelper

  protected val testDataContext: TestDataContext
    get() = TestDataContext(webTestClient, jwtAuthenticationHelper, dataLoader)

  protected fun createHttpEntity(bearerToken: String?, body: Any?): HttpEntity<*> {
    return createHttpEntity(bearerToken, body, emptyMap<String?, String>())
  }

  protected fun createHttpEntity(authToken: AuthToken, body: Any?): HttpEntity<*> {
    return createHttpEntity(authTokenHelper.getToken(authToken), body, emptyMap<String?, String>())
  }

  protected fun createEmptyHttpEntity(authToken: AuthToken): HttpEntity<*> {
    return createHttpEntity(authTokenHelper.getToken(authToken), null, emptyMap<String?, String>())
  }

  protected fun createEmptyHttpEntity(authToken: AuthToken, additionalHeaders: Map<String?, String?>): HttpEntity<*> {
    return createHttpEntity(authTokenHelper.getToken(authToken), null, additionalHeaders)
  }

  protected fun createHttpEntity(
    bearerToken: String?,
    body: Any?,
    additionalHeaders: Map<String?, String?> = emptyMap<String?, String>(),
  ): HttpEntity<*> {
    val headers = HttpHeaders()
    headers.add("Authorization", "Bearer $bearerToken")
    headers.add("Content-Type", "application/json")
    headers.add("Accept", "application/json")
    additionalHeaders.forEach { (headerName: String?, headerValue: String?) -> headers.add(headerName, headerValue) }
    return HttpEntity(body, headers)
  }

  protected fun createHttpEntityWithBearerAuthorisationAndBody(
    user: String?,
    roles: List<String>,
    body: Any?,
  ): HttpEntity<*> {
    val jwt = createJwt(user, roles)
    return createHttpEntity(jwt, body)
  }

  protected fun createHttpEntityWithBearerAuthorisation(
    user: String?,
    roles: List<String>,
    additionalHeaders: Map<String?, String?>?,
  ): HttpEntity<*> {
    val jwt = createJwt(user, roles)
    return createHttpEntity(jwt, null, additionalHeaders ?: java.util.Map.of())
  }

  protected fun createJwt(user: String?, roles: List<String>): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username(user!!)
        .roles(roles)
        .scope(listOf("read", "write"))
        .expiryTime(Duration.ofDays(1))
        .build(),
    )
  }

  protected fun validToken(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .scope(listOf("read", "write"))
        .roles(listOf())
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  protected fun validToken(roles: List<String>): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .scope(listOf("read", "write"))
        .roles(roles)
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  protected fun readOnlyToken(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("ITAG_USER")
        .scope(listOf("read"))
        .roles(listOf())
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  protected fun clientToken(roles: List<String>): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .clientId("api-client-id")
        .scope(listOf("read", "write"))
        .roles(roles)
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  protected fun <T> assertThatStatus(response: ResponseEntity<T>, status: Int) {
    assertThatStatus(response, HttpStatusCode.valueOf(status))
  }

  protected fun <T> assertThatStatus(response: ResponseEntity<T>, status: HttpStatusCode?) {
    Assertions.assertThat(response.statusCode).withFailMessage(
      "Expecting status code value <%s> to be equal to <%s> but it was not.\nBody was\n%s",
      response.statusCode,
      status,
      response.body,
    ).isEqualTo(status)
  }

  protected fun assertThatJsonFileAndStatus(response: ResponseEntity<String?>, status: Int, jsonFile: String?) {
    assertThatJsonFileAndStatus(response, HttpStatusCode.valueOf(status), jsonFile)
  }

  protected fun assertThatJsonFileAndStatus(
    response: ResponseEntity<String?>,
    status: HttpStatusCode?,
    jsonFile: String?,
  ) {
    assertThatStatus(response, status)
    val bodyAsJsonContent = getBodyAsJsonContent<Any>(response)
    Assertions.assertThat(bodyAsJsonContent).isEqualToJson(jsonFile)
  }

  protected fun assertThatJsonAndStatus(response: ResponseEntity<String?>, status: Int, json: String?) {
    assertThatStatus(response, status)
    JsonAssertions.assertThatJson(response.body).isEqualTo(json)
  }

  protected fun assertThatOKResponseContainsJson(response: ResponseEntity<String?>, json: String?) {
    assertThatStatus(response, 200)
    Assertions.assertThat(getBodyAsJsonContent<Any>(response)).isEqualToJson(json)
  }

  protected fun <T> getBodyAsJsonContent(response: ResponseEntity<String?>): JsonContent<T> {
    return JsonContent(
      javaClass,
      ResolvableType.forType(
        String::class.java,
      ),
      Objects.requireNonNull(response.body),
    )
  }

  protected fun setAuthorisation(roles: List<String>): Consumer<HttpHeaders> {
    return Consumer { httpHeaders: HttpHeaders -> httpHeaders.add("Authorization", "Bearer " + validToken(roles)) }
  }

  protected fun setAuthorisation(username: String?, roles: List<String>): Consumer<HttpHeaders> {
    return Consumer { httpHeaders: HttpHeaders ->
      httpHeaders.add(
        "Authorization",
        "Bearer " + createJwt(username, roles),
      )
    }
  }

  protected fun setClientAuthorisation(roles: List<String>): Consumer<HttpHeaders> {
    return Consumer { httpHeaders: HttpHeaders -> httpHeaders.add("Authorization", "Bearer " + clientToken(roles)) }
  }
}
