package uk.gov.justice.hmpps.prison.api.resource.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.api.model.SentenceSummary

class PrisonResourceTest : ResourceTest() {

  @Autowired
  val objectMapper = jacksonObjectMapper()

  @Test
  fun `Test that endpoint returns a summary list when authorised`() {
    val establishment = "LEI"

    val json = getPrisonResourceAsText("prison_resource_single_sentencesummary.json")
    val sentenceSummary: SentenceSummary = objectMapper.readValue(json)

    webTestClient.get()
      .uri("/api/prison/{establishment}/booking/latest/sentence-summary", establishment)
      .headers(
        setAuthorisation(
          listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBodyList(object : ParameterizedTypeReference<SentenceSummary>() {})
      .contains(sentenceSummary)
  }

  @Test
  fun `Test that endpoint returns a forbidden when unauthorised`() {
    val establishment = "LEI"

    webTestClient.get()
      .uri("/api/prison/{establishment}/booking/latest/sentence-summary", establishment)
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
