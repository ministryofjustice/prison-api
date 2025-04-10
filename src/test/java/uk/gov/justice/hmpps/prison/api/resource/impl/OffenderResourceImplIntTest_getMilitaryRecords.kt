@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod.GET
import org.springframework.test.context.bean.override.mockito.MockitoBean
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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderMilitaryRecordRepository
import java.time.LocalDate
import java.util.Optional

class OffenderResourceImplIntTest_getMilitaryRecords : ResourceTest() {

  private fun stubRepositoryCall() {
    whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(anyString(), anyInt())).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .bookingId(-1L)
          .build(),
      ),
    )

    whenever(offenderMilitaryRecordRepository.findAllByBookingId(anyLong())).thenReturn(
      listOf(
        OffenderMilitaryRecord.builder()
          .bookingAndSequence(OffenderMilitaryRecord.BookingAndSequence(OffenderBooking.builder().bookingId(-1L).build(), 1))
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
          .bookingAndSequence(OffenderMilitaryRecord.BookingAndSequence(OffenderBooking.builder().bookingId(-1L).build(), 2))
          .startDate(LocalDate.parse("2001-01-01"))
          .militaryBranch(MilitaryBranch("NAV", "Navy"))
          .description("second record")
          .build(),
      ),
    )
  }

  @MockitoBean
  private lateinit var offenderBookingRepository: OffenderBookingRepository

  @MockitoBean
  private lateinit var offenderMilitaryRecordRepository: OffenderMilitaryRecordRepository

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
  fun shouldReturn403WhenNoPrivileges() {
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
        .status(403)
        .userMessage("Unauthorised access to booking with id -1.")
        .build(),
    )
    verifyNoInteractions(offenderBookingRepository)
  }
}
