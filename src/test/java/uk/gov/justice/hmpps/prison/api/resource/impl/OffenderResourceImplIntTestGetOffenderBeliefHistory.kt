package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBelief
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBeliefRepository
import java.time.LocalDateTime

class OffenderResourceImplIntTestGetOffenderBeliefHistory : ResourceTest() {

  private val beliefCode = ProfileCode.builder().id(ProfileCode.PK(ProfileType.builder().type("RELF").build(), "SCIE")).description("Scientologist").build()
  private val staffUserAccount = StaffUserAccount.builder()
    .username("johnsmith")
    .staff(
      Staff.builder()
        .firstName("John")
        .lastName("Smith")
        .build(),
    )
    .build()
  private val offenderBelief = OffenderBelief(
    98765L, OffenderBooking.builder().bookingId(123456L).build(),
    beliefCode, LocalDateTime.parse("2024-01-01T00:00:00"), null, true, "Comments",
    false, LocalDateTime.parse("2024-01-01T00:00:00"), staffUserAccount, null, null,
  )
  private fun stubRepositoryCall() {
    whenever(offenderBeliefRepository.getOffenderBeliefHistory(anyString(), anyOrNull())).thenReturn(
      listOf(
        offenderBelief,
      ),
    )
  }

  @MockBean
  private lateinit var offenderBeliefRepository: OffenderBeliefRepository

  @Test
  fun `should return 401 when user does not even have token`() {
    webTestClient.get().uri("/api/offenders/B1101BB/belief-history")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if no override role`() {
    webTestClient.get().uri("/api/offenders/B1101BB/belief-history")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
    stubRepositoryCall()
    webTestClient.get().uri("/api/offenders/B1101BB/belief-history")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun shouldReturnOffenderBeliefHistory() {
    stubRepositoryCall()
    webTestClient.get().uri("/api/offenders/B1101BB/belief-history")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectBody()
      .jsonPath("[0].beliefId").isEqualTo(98765)
      .jsonPath("[0].bookingId").isEqualTo(123456)
      .jsonPath("[0].beliefCode").isEqualTo("SCIE")
      .jsonPath("[0].beliefDescription").isEqualTo("Scientologist")
      .jsonPath("[0].startDate").isEqualTo("2024-01-01")
      .jsonPath("[0].changeReason").isEqualTo(true)
      .jsonPath("[0].comments").isEqualTo("Comments")
      .jsonPath("[0].addedByFirstName").isEqualTo("John")
      .jsonPath("[0].addedByLastName").isEqualTo("Smith")
      .jsonPath("[0].verified").isEqualTo(false)
  }
}
