@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.api.model.calculation.CalculableSentenceEnvelope
import uk.gov.justice.hmpps.prison.api.model.calculation.CalculableSentenceEnvelopeVersion2
import java.time.LocalDate

@DisplayName("GET /api/bookings/latest/calculable-sentence-envelope")
class BookingResourceIntTest_getCalculableSentenceEnvelope : ResourceTest() {

  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.get().uri("/api/bookings/latest/calculable-sentence-envelope")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `returns 403 when client does not have authorised role`() {
    webTestClient.get().uri("/api/bookings/latest/calculable-sentence-envelope?offenderNo=A1234AB")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns 403 when client has authorised role ROLE_RELEASE_DATE_MANUAL_COMPARER and no user in context`() {
    webTestClient.get().uri("/api/bookings/latest/calculable-sentence-envelope?offenderNo=A1234AB")
      .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns 403 when client has authorised role ROLE_RELEASE_DATE_MANUAL_COMPARER and SYSTEM_USER override role`() {
    webTestClient.get().uri("/api/bookings/latest/calculable-sentence-envelope?offenderNo=A1234AB")
      .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER", "ROLE_SYSTEM_USER")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns 200 when client has authorised role ROLE_RELEASE_DATE_MANUAL_COMPARER and VIEW_PRISONER_DATA override role`() {
    webTestClient.get().uri("/api/bookings/latest/calculable-sentence-envelope?offenderNo=A1234AB")
      .headers(setClientAuthorisation(listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER", "ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns 404 when user does not have offender in caseload`() {
    webTestClient.get().uri("/api/bookings/latest/calculable-sentence-envelope?offenderNo=A1234AB")
      .headers(setAuthorisation("WAI_USER", listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER")))
      .exchange()
      .expectStatus().isForbidden
      .expectBody().jsonPath("userMessage").isEqualTo("Unauthorised access to booking with id -2.")
  }

  @Test
  fun `returns success when user has offender in caseload`() {
    webTestClient.get().uri("/api/bookings/latest/calculable-sentence-envelope?offenderNo=A1234AB")
      .headers(setAuthorisation(listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `Test that endpoint returns a summary list when authorised`() {
    val json = getPrisonResourceAsText("prison_resource_single_calculable_sentence_envelope.json")
    val calculableSentenceEnvelope = objectMapper.readValue<CalculableSentenceEnvelope>(json)
    val fixedRecallCalculableSentenceEnvelope = objectMapper.readValue<CalculableSentenceEnvelope>(getPrisonResourceAsText("prison_resource_fixed_recall_calculable_sentence_envelope.json"))
    fixedRecallCalculableSentenceEnvelope.person.alerts.forEach { it.dateCreated = LocalDate.now() }

    webTestClient.get()
      .uri { uriBuilder ->
        uriBuilder.path("/api/bookings/latest/calculable-sentence-envelope")
          .queryParam("offenderNo", "A1234AB", "A1234AE")
          .build()
      }
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

  @Test
  fun `Test that version 2 endpoint returns a summary list when authorised`() {
    val json = getPrisonResourceAsText("prison_resource_single_calculable_sentence_envelope_v2.json")
    val calculableSentenceEnvelope = objectMapper.readValue<CalculableSentenceEnvelopeVersion2>(json)
    val fixedRecallCalculableSentenceEnvelope = objectMapper.readValue<CalculableSentenceEnvelopeVersion2>(getPrisonResourceAsText("prison_resource_fixed_recall_calculable_sentence_envelope_v2.json"))

    webTestClient.get()
      .uri { uriBuilder ->
        uriBuilder.path("/api/bookings/latest/calculable-sentence-envelope/v2")
          .queryParam("offenderNo", "A1234AB", "A1234AE")
          .build()
      }
      .headers(
        setAuthorisation(
          listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBodyList(object : ParameterizedTypeReference<CalculableSentenceEnvelopeVersion2>() {})
      .contains(calculableSentenceEnvelope, fixedRecallCalculableSentenceEnvelope)
  }

  @Test
  fun `Test that endpoint returns a forbidden when unauthorised`() {
    webTestClient.get()
      .uri { uriBuilder ->
        uriBuilder.path("/api/bookings/latest/calculable-sentence-envelope")
          .queryParam("offenderNo", "Z0020ZZ", "A1234AE")
          .build()
      }
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

  private fun getPrisonResourceAsText(path: String): String = getResourceAsText("/${this.javaClass.`package`.name.replace(".", "/")}/$path")

  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  private fun getResourceAsText(path: String): String = object {}.javaClass.getResource(path).readText()
}
