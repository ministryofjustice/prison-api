@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.repository.OffenderDeletionRepository
import uk.gov.justice.hmpps.prison.service.OffenderLocation
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder

@DisplayName("GET /offenders/{offenderNo}/housing-location")
class OffenderResourceIntTest_getHousingLocation : ResourceTest() {
  @Autowired
  lateinit var offenderDeletionRepository: OffenderDeletionRepository

  @Test
  internal fun `404 when offender not found`() {
    val offenderNo = "Z9999ZZ"

    webTestClient.get()
      .uri("/api/offenders/{offenderNo}/housing-location", offenderNo)
      .headers(
        setAuthorisation(
          listOf("ROLE_SYSTEM_USER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  internal fun `empty response when offender not in prison`() {
    val offenderNo =
      OffenderBuilder().withBooking(OffenderBookingBuilder(released = true)).save(testDataContext).offenderNo

    val location = webTestClient.get()
      .uri("/api/offenders/{offenderNo}/housing-location", offenderNo)
      .headers(
        setAuthorisation(
          listOf("ROLE_SYSTEM_USER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .returnResult(OffenderLocation::class.java)
      .responseBody.blockFirst()!!

    assertThat(location).isEqualTo(OffenderLocation())

    // tidy up
    offenderDeletionRepository.deleteAllOffenderDataIncludingBaseRecord(offenderNo)
  }

  @Test
  internal fun `response with levels for offender`() {
    val offenderNo = OffenderBuilder().withBooking(
      OffenderBookingBuilder(cellLocation = "LEI-A-1-10", prisonId = "LEI"),
    ).save(testDataContext).offenderNo

    webTestClient.get()
      .uri("/api/offenders/{offenderNo}/housing-location", offenderNo)
      .headers(
        setAuthorisation(
          listOf("ROLE_SYSTEM_USER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("levels[*].level").isEqualTo(JSONArray().also { it.addAll(listOf(1, 2, 3)) })
      .jsonPath("levels[*].code").isEqualTo(JSONArray().also { it.addAll(listOf("A", "1", "10")) })
      .jsonPath("levels[*].type").isEqualTo(JSONArray().also { it.addAll(listOf("WING", "LAND", "CELL")) })
      .jsonPath("levels[*].description").isEqualTo(JSONArray().also { it.addAll(listOf("Block A", "Landing A/1", "Cell 10")) })
      .jsonPath("lastPermanentLevels").doesNotExist()

    // tidy up
    offenderDeletionRepository.deleteAllOffenderDataIncludingBaseRecord(offenderNo)
  }

  @Test
  internal fun `response with levels for offender in reception`() {
    val offenderNo = OffenderBuilder().withBooking(
      OffenderBookingBuilder(cellLocation = "LEI-RECP", prisonId = "LEI"),
    ).save(testDataContext).offenderNo

    webTestClient.get()
      .uri("/api/offenders/{offenderNo}/housing-location", offenderNo)
      .headers(
        setAuthorisation(
          listOf("ROLE_SYSTEM_USER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("levels[*].level").isEqualTo(JSONArray().also { it.addAll(listOf(1)) })
      .jsonPath("levels[*].code").isEqualTo(JSONArray().also { it.addAll(listOf("RECP")) })
      .jsonPath("levels[0].type").doesNotExist()
      .jsonPath("levels[*].description").isEqualTo(JSONArray().also { it.addAll(listOf("RECP")) })
      .jsonPath("lastPermanentLevels").doesNotExist()

    offenderDeletionRepository.deleteAllOffenderDataIncludingBaseRecord(offenderNo)
  }
}
