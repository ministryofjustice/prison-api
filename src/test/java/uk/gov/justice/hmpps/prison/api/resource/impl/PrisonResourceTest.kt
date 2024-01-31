package uk.gov.justice.hmpps.prison.api.resource.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD
import org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED
import uk.gov.justice.hmpps.prison.api.model.calculation.CalculableSentenceEnvelope
import java.time.LocalDate

@DisplayName("GET /api/prison/{establishmentId}/booking/latest/paged/calculable-sentence-envelope")
class PrisonResourceTest : ResourceTest() {

  @Autowired
  val objectMapper = jacksonObjectMapper()

  @Sql(
    scripts = ["/sql/create_offender_details_used_for_calc.sql"],
    executionPhase = BEFORE_TEST_METHOD,
    config = SqlConfig(transactionMode = ISOLATED),
  )
  @Sql(
    scripts = ["/sql/clean_offender_details_used_for_calc.sql"],
    executionPhase = AFTER_TEST_METHOD,
    config = SqlConfig(transactionMode = ISOLATED),
  )
  @Test
  fun `Test that paginated endpoint returns the correct calculable sentence and the correct number of pages`() {
    val establishment = "LEI"

    val json = getPrisonResourceAsText("prison_resource_single_calculable_sentence_envelope.json")
    val calculableSentenceEnvelope = objectMapper.readValue<CalculableSentenceEnvelope>(json)

    val fixedRecallCalculableSentenceEnvelope = objectMapper.readValue<CalculableSentenceEnvelope>(getPrisonResourceAsText("prison_resource_fixed_recall_calculable_sentence_envelope.json"))
    fixedRecallCalculableSentenceEnvelope.person.alerts.forEach { it.dateCreated = LocalDate.now() }

    val firstPageResponse = webTestClient.get()
      .uri("/api/prison/{establishment}/booking/latest/paged/calculable-sentence-envelope?page=0&size=3", establishment)
      .headers(
        setAuthorisation(
          listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(object : ParameterizedTypeReference<RestResponsePage<CalculableSentenceEnvelope>>() {})
      .returnResult()
      .responseBody!!

    assertThat(firstPageResponse.pageable.pageNumber).isEqualTo(0)
    assertThat(firstPageResponse.totalPages).isEqualTo(2)
    assertFalse(firstPageResponse.isLast)
    assertThat(firstPageResponse.content).contains(calculableSentenceEnvelope, fixedRecallCalculableSentenceEnvelope)

    val secondPageResponse = webTestClient.get()
      .uri("/api/prison/{establishment}/booking/latest/paged/calculable-sentence-envelope?page=1&size=3", establishment)
      .headers(
        setAuthorisation(
          listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(object : ParameterizedTypeReference<RestResponsePage<CalculableSentenceEnvelope>>() {})
      .returnResult()
      .responseBody!!

    assertThat(secondPageResponse.pageable.pageNumber).isEqualTo(1)
    assertThat(secondPageResponse.totalPages).isEqualTo(2)
    assertTrue(secondPageResponse.isLast)
    assertThat(secondPageResponse.content.size).isEqualTo(1)
  }

  private fun getPrisonResourceAsText(path: String): String {
    return getResourceAsText("/${this.javaClass.`package`.name.replace(".", "/")}/$path")
  }

  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  private fun getResourceAsText(path: String): String {
    return object {}.javaClass.getResource(path).readText()
  }

  @Nested
  inner class Authorisation {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/prison/LEI/booking/latest/paged/calculable-sentence-envelope")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not authorised role and override role`() {
      webTestClient.get().uri("/api/prison/LEI/booking/latest/paged/calculable-sentence-envelope")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 if has authorised role and SYSTEM_USER override role`() {
      webTestClient.get().uri("/api/prison/LEI/booking/latest/paged/calculable-sentence-envelope")
        .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER", "SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 200 if has authorised role and override role`() {
      webTestClient.get().uri("/api/prison/LEI/booking/latest/paged/calculable-sentence-envelope")
        .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER", "VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return 404 if does not have prison in caseload`() {
      webTestClient.get().uri("/api/prison/LEI/booking/latest/paged/calculable-sentence-envelope")
        .headers(setAuthorisation("WAI_USER", listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 200 if has prison in caseload`() {
      webTestClient.get().uri("/api/prison/LEI/booking/latest/paged/calculable-sentence-envelope")
        .headers(setAuthorisation(listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER")))
        .exchange()
        .expectStatus().isOk
    }
  }
}
