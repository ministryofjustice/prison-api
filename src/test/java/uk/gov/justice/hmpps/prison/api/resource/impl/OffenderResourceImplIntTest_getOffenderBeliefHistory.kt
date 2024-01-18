package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod.GET
import uk.gov.justice.hmpps.prison.repository.jpa.model.*
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBeliefRepository
import java.time.LocalDateTime

class OffenderResourceImplIntTest_getOffenderBeliefHistory : ResourceTest() {

  private val beliefCode = ProfileCode.builder().id(ProfileCode.PK(ProfileType.builder().type("RELF").build(), "SCIE")).build()
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
    beliefCode, LocalDateTime.parse("2024-01-01"), null, true, "Comments",
    false, LocalDateTime.parse("2024-01-01"), staffUserAccount, null, null,
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
    webTestClient.get().uri("/api/offenders/A1234AA/belief-history")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if no override role`() {
    webTestClient.get().uri("/api/offenders/A1234AA/belief-history")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
    stubRepositoryCall()
    webTestClient.get().uri("/api/offenders/A1234AA/belief-history")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun shouldReturnOffenderBeliefHistory() {
    stubRepositoryCall()
    val responseEntity = testRestTemplate.exchange(
      "/api/offenders/A1234AA/belief-history",
      GET,
      createHttpEntity(validToken(listOf("ROLE_VIEW_PRISONER_DATA")), null),
      String::class.java,
    )
    assertThatJsonFileAndStatus(responseEntity, 200, "offender_belief_history.json")
  }
}
