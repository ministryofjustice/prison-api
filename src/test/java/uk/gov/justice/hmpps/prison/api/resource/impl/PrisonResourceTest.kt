package uk.gov.justice.hmpps.prison.api.resource.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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

class PrisonResourceTest : ResourceTest() {

  @Autowired
  val objectMapper = jacksonObjectMapper()

  @Test
  fun `Test that endpoint returns a summary list when authorised`() {
    val establishment = "LEI"

    val json = getPrisonResourceAsText("prison_resource_single_calculable_sentence_envelope.json")
    val calculableSentenceEnvelope = objectMapper.readValue<CalculableSentenceEnvelope>(json)

    val fixedRecallCalculableSentenceEnvelope = objectMapper.readValue<CalculableSentenceEnvelope>(getPrisonResourceAsText("prison_resource_fixed_recall_calculable_sentence_envelope.json"))
    fixedRecallCalculableSentenceEnvelope.person.alerts.forEach { it.dateCreated = LocalDate.now() }

    webTestClient.get()
      .uri("/api/prison/{establishment}/booking/latest/calculable-sentence-envelope", establishment)
      .headers(
        setAuthorisation(
          listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBodyList(object : ParameterizedTypeReference<CalculableSentenceEnvelope>() {})
      .contains(calculableSentenceEnvelope, fixedRecallCalculableSentenceEnvelope)
  }

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

  @Test
  fun `Test that endpoint returns a forbidden when unauthorised`() {
    val establishment = "LEI"

    webTestClient.get()
      .uri("/api/prison/{establishment}/booking/latest/calculable-sentence-envelope", establishment)
      .headers(
        setAuthorisation(
          listOf("ROLE_RELEASE_DATES_CALCULATOR"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isForbidden
  }

  private fun getPrisonResourceAsText(path: String): String {
    return getResourceAsText("/${this.javaClass.`package`.name.replace(".", "/")}/$path")
  }

  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  private fun getResourceAsText(path: String): String {
    return object {}.javaClass.getResource(path).readText()
  }
}
