@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderDamageObligationRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional

class OffenderResourceImplIntTest_bookingDamageObligations : ResourceTest() {
  private var token: String? = null

  @MockBean
  private lateinit var offenderDamageObligationRepository: OffenderDamageObligationRepository

  @MockBean
  private lateinit var bookingRepository: BookingRepository

  @BeforeEach
  fun setup() {
    token = authTokenHelper.getToken(NORMAL_USER)
  }

  @Test
  fun retrieveOffenderTransactionHistory() {
    stubVerifyOffenderAccess("A12345")
    stubSuccess()
    val request = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/offenders/{offenderNo}/damage-obligations",
      GET,
      request,
      object : ParameterizedTypeReference<String?>() {},
      "A12345",
    )
    assertThatJsonFileAndStatus(response, 200, "offender_damage_obligations.json")
  }

  @Test
  fun offenderNotFound() {
    whenever(offenderDamageObligationRepository.findOffenderDamageObligationByOffender_NomsId(any())).thenThrow(EntityNotFoundException())
    val request = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/offenders/{offenderNo}/damage-obligations",
      GET,
      request,
      object : ParameterizedTypeReference<String?>() {},
      "A12345",
    )
    assertThat(response.statusCode.value()).isEqualTo(404)
  }

  @Test
  fun offenderInADifferentCaseLoad() {
    val request = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/offenders/{offenderNo}/damage-obligations",
      GET,
      request,
      object : ParameterizedTypeReference<String?>() {},
      "Z00028",
    )
    assertThat(response.statusCode.value()).isEqualTo(404)
  }

  private fun stubVerifyOffenderAccess(offenderNo: String) {
    whenever(bookingRepository.getLatestBookingIdentifierForOffender(any()))
      .thenReturn(Optional.of(OffenderBookingIdSeq(offenderNo, 1L, 1)))
    whenever(bookingRepository.verifyBookingAccess(any(), any())).thenReturn(true)
  }

  private fun stubSuccess() {
    whenever(offenderDamageObligationRepository.findOffenderDamageObligationByOffender_NomsId(any())).thenReturn(
      listOf(
        OffenderDamageObligation.builder()
          .id(1L)
          .comment("Damages made to canteen furniture")
          .amountPaid(BigDecimal.valueOf(500))
          .amountToPay(BigDecimal.ZERO)
          .startDateTime(LocalDateTime.parse("2020-10-10T10:00"))
          .endDateTime(LocalDateTime.parse("2020-10-22T10:00"))
          .offender(Offender.builder().nomsId("A12345").build())
          .referenceNumber("123")
          .status("ACTIVE")
          .prison(AgencyLocation.builder().id("MDI").description("Moorland").build())
          .build(),
      ),
    )
  }

  @Test
  fun when_NoStatus_Then_DefaultToALL() {
    stubVerifyOffenderAccess("A12345")
    val request = createHttpEntity(token, null)
    testRestTemplate.exchange(
      "/api/offenders/{offenderNo}/damage-obligations",
      GET,
      request,
      object : ParameterizedTypeReference<String?>() {},
      "A12345",
    )
    verify(offenderDamageObligationRepository, times(1)).findOffenderDamageObligationByOffender_NomsId("A12345")
    verify(offenderDamageObligationRepository, times(0)).findOffenderDamageObligationByOffender_NomsIdAndStatus(anyString(), anyString())
  }

  @Test
  fun when_StatusIsACTIVE_Then_DoNotDefaultToALL() {
    stubVerifyOffenderAccess("A12345")
    val request = createHttpEntity(token, null)
    testRestTemplate.exchange(
      "/api/offenders/{offenderNo}/damage-obligations?status=ACTIVE",
      GET,
      request,
      object : ParameterizedTypeReference<String?>() {},
      "A12345",
    )
    verify(offenderDamageObligationRepository, times(0)).findOffenderDamageObligationByOffender_NomsId("A12345")
    verify(offenderDamageObligationRepository, times(1)).findOffenderDamageObligationByOffender_NomsIdAndStatus("A12345", "ACTIVE")
  }

  @Test
  fun when_StatusIsBadValue_Then_DefaultToALL() {
    stubVerifyOffenderAccess("A12345")
    val request = createHttpEntity(token, null)
    testRestTemplate.exchange(
      "/api/offenders/{offenderNo}/damage-obligations?status=BADSTATUS",
      GET,
      request,
      object : ParameterizedTypeReference<String?>() {},
      "A12345",
    )
    verify(offenderDamageObligationRepository, times(1)).findOffenderDamageObligationByOffender_NomsId("A12345")
    verify(offenderDamageObligationRepository, times(0)).findOffenderDamageObligationByOffender_NomsIdAndStatus(anyString(), anyString())
  }
}
