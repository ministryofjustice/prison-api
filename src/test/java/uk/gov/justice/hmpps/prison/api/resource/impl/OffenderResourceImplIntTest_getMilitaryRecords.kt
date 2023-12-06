package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod.GET
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse.builder
import uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord
import uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import java.time.LocalDate
import java.util.Optional

class OffenderResourceImplIntTest_getMilitaryRecords : ResourceTest() {

  private fun stubRepositoryCall() {
    whenever(offenderBookingRepository.findById(anyLong())).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .militaryRecords(
            listOf(
              OffenderMilitaryRecord.builder()
                .startDate(LocalDate.parse("2000-01-01"))
                .endDate(LocalDate.parse("2020-10-17"))
                .militaryDischarge(MilitaryDischarge("DIS", "Dishonourable"))
                .warZone(WarZone("AFG", "Afghanistan"))
                .militaryBranch(MilitaryBranch("ARM", "Army"))
                .description("left")
                .unitNumber("auno")
                .enlistmentLocation("Somewhere")
                .militaryRank(MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)"))
                .serviceNumber("asno")
                .disciplinaryAction(DisciplinaryAction("CM", "Court Martial"))
                .dischargeLocation("Sheffield")
                .build(),
              OffenderMilitaryRecord.builder()
                .startDate(LocalDate.parse("2001-01-01"))
                .militaryBranch(MilitaryBranch("NAV", "Navy"))
                .description("second record")
                .build(),
            ),
          )
          .build(),
      ),
    )
  }

  @MockBean
  private lateinit var offenderBookingRepository: OffenderBookingRepository

  @Test
  fun `should return 401 when user does not even have token`() {
    webTestClient.get().uri("/api/offenders/A1234AA/military-records")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if no override role`() {
    webTestClient.get().uri("/api/offenders/A1234AA/military-records")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
    stubRepositoryCall()
    webTestClient.get().uri("/api/offenders/A1234AA/military-records")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun shouldReturnMilitaryRecords() {
    stubRepositoryCall()
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange(
      "/api/offenders/A1234AA/military-records",
      GET,
      requestEntity,
      String::class.java,
    )
    assertThatJsonFileAndStatus(responseEntity, 200, "military_records.json")
  }

  @Test
  fun shouldReturn404WhenOffenderNotFound() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val response = testRestTemplate.exchange(
      "/api/offenders/A1554AN/military-records",
      GET,
      requestEntity,
      ErrorResponse::class.java,
    )
    assertThat(response.body).isEqualTo(
      builder()
        .status(404)
        .userMessage("Resource with id [A1554AN] not found.")
        .developerMessage("Resource with id [A1554AN] not found.")
        .build(),
    )
  }

  @Test
  fun shouldReturn404WhenNoPrivileges() {
    // run with user that doesn't have access to the caseload
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", listOf(), mapOf())
    val response = testRestTemplate.exchange(
      "/api/offenders/A1234AA/military-records",
      GET,
      requestEntity,
      ErrorResponse::class.java,
    )
    assertThat(response.body).isEqualTo(
      builder()
        .status(404)
        .userMessage("Resource with id [A1234AA] not found.")
        .developerMessage("Resource with id [A1234AA] not found.")
        .build(),
    )
    verifyNoInteractions(offenderBookingRepository)
  }
}
