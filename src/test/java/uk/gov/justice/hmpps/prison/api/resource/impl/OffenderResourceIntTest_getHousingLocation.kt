@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import net.minidev.json.JSONArray
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.repository.OffenderDeletionRepository
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
  fun `should return 401 when user does not even have token`() {
    webTestClient.get().uri("/api/offenders/{offenderNo}/housing-location", "A1234AA")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 as endpoint does not have override role`() {
    webTestClient.get().uri("/api/offenders/{offenderNo}/housing-location", "A1234AA")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  internal fun `404 response when offender not in prison`() {
    val offenderNo =
      OffenderBuilder().withBooking(OffenderBookingBuilder(released = true)).save(testDataContext).offenderNo

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
      .jsonPath("levels[*].description").isEqualTo(JSONArray().also { it.addAll(listOf("Block A", "LANDING A/1", "Cell 10")) })
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
