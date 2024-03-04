@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.service.BedAssignmentHistoryService
import uk.gov.justice.hmpps.prison.util.JwtParameters.Companion.builder
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.util.Arrays.stream

@ContextConfiguration(classes = [BookingMovementsResourceIntTest_moveToCell.TestClock::class])
class BookingMovementsResourceIntTest_moveToCellSwap : ResourceTest() {
  @TestConfiguration
  internal class TestClock {
    @Bean
    fun clock(): Clock = LocalDate.parse("2020-10-01", ISO_DATE).atStartOfDay()
      .run { Clock.fixed(this.toInstant(ZoneOffset.UTC), ZoneId.systemDefault()) }
  }

  @Autowired
  private lateinit var offenderBookingRepository: OffenderBookingRepository

  @Autowired
  private lateinit var bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository

  @Autowired
  private lateinit var clock: Clock

  @SpyBean
  private lateinit var bedAssignmentHistoryService: BedAssignmentHistoryService

  @AfterEach
  fun tearDown() {
    // Return the offender back to his original cell as configured in the test data in R__3_6_1__OFFENDER_BOOKINGS.sql
    requestMoveToCell(bearerToken = validToken(), dateTime = INITIAL_DATE_TIME.format(ISO_LOCAL_DATE_TIME))
  }

  @Test
  fun validRequest() {
    val dateTime = now().minusHours(1)

    val response = requestMoveToCellSwap(validToken(), BOOKING_ID_S, "BEH", dateTime.format(ISO_LOCAL_DATE_TIME))

    verifySuccessResponse(response = response)
    verifyOffenderBookingLivingUnit(livingUnitId = NEW_CELL)
    verifyLastBedAssignmentHistory(reason = "BEH", dateTime = dateTime)
  }

  @Test
  fun validRequest_withoutReasonCode_defaultsToAdm() {
    val dateTime = now().minusHours(1)

    val response = requestMoveToCellSwap(validToken(), BOOKING_ID_S, null, dateTime.format(ISO_LOCAL_DATE_TIME))

    verifySuccessResponse(response = response)
    verifyOffenderBookingLivingUnit(livingUnitId = NEW_CELL)
    verifyLastBedAssignmentHistory(reason = "ADM", dateTime = dateTime)
  }

  @Test
  fun missingDate_defaultsToNow() {
    val expectedDateTime = clock.instant().atZone(ZoneId.systemDefault()).toLocalDateTime()

    val response = requestMoveToCellSwap(validToken(), BOOKING_ID_S, "BEH", "")

    verifySuccessResponse(response = response)
    verifyOffenderBookingLivingUnit(livingUnitId = NEW_CELL)
    verifyLastBedAssignmentHistory(reason = "BEH", dateTime = expectedDateTime)
  }

  @Test
  fun notFound() {
    val dateTime = now().minusHours(1)
    val invalidBookingId = "-69854"

    val response = requestMoveToCellSwap(validToken(), invalidBookingId, "BEH", dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME))

    verifyErrorResponse(response, NOT_FOUND, invalidBookingId)
    verifyOffenderBookingLivingUnit()
    verifyLastBedAssignmentHistory()
  }

  @Test
  fun noBookingAccess_notFound() {
    val dateTime = now().minusHours(1)

    val response = requestMoveToCellSwap(differentAgencyToken(), BOOKING_ID_S, "BEH", dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME))

    verifyErrorResponse(response, NOT_FOUND, BOOKING_ID_S)
    verifyOffenderBookingLivingUnit()
    verifyLastBedAssignmentHistory()
  }

  @Test
  fun overrideNoBookingAccess_Success() {
    val dateTime = now().minusHours(1)

    val response = requestMoveToCellSwap(noUserInContext(), BOOKING_ID_S, "BEH", dateTime.format(ISO_LOCAL_DATE_TIME))

    verifySuccessResponse(response = response)
    verifyOffenderBookingLivingUnit(livingUnitId = NEW_CELL)
    verifyLastBedAssignmentHistory(reason = "BEH", dateTime = dateTime)
  }

  @Test
  fun userReadOnly_forbidden() {
    val dateTime = now().minusHours(1)

    val response = requestMoveToCellSwap(readOnlyToken(), BOOKING_ID_S, "BEH", dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME))

    verifyErrorResponse(response, FORBIDDEN, "")
    verifyOffenderBookingLivingUnit()
    verifyLastBedAssignmentHistory()
  }

  @Test
  @WithMockUser(username = "ITAG_USER", authorities = ["SCOPE_write"]) // Required because stubbing the BedAssignmentHistoryService means we don't pick up the usual Authentication from Spring AOP.
  fun transactionRolledBack() {
    val dateTime = now().minusHours(1)

    doThrow(RuntimeException::class.java).`when`(bedAssignmentHistoryService).add(BOOKING_ID, NEW_CELL, "BEH", dateTime)
    val response = requestMoveToCellSwap(validToken(), BOOKING_ID_S, "BEH", dateTime.format(ISO_LOCAL_DATE_TIME))

    verifyErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "")
    verifyOffenderBookingLivingUnit()
    verifyLastBedAssignmentHistory()
  }

  private fun differentAgencyToken(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("WAI_USER")
        .scope(listOf("read", "write"))
        .roles(listOf())
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  private fun noUserInContext(): String {
    return jwtAuthenticationHelper.createJwt(
      builder()
        .username("a-system-client-id")
        .scope(listOf("read", "write"))
        .roles(listOf("MAINTAIN_CELL_MOVEMENTS"))
        .expiryTime(Duration.ofDays((365 * 10).toLong()))
        .build(),
    )
  }

  // Type on ParameterizedTypeReference required to work around https://bugs.openjdk.java.net/browse/JDK-8210197
  private fun requestMoveToCellSwap(bearerToken: String, bookingId: String, reasonCode: String?, dateTime: String): ResponseEntity<String> {
    val body = if (reasonCode != null) mapOf("reasonCode" to reasonCode, "dateTime" to dateTime) else mapOf("dateTime" to dateTime)

    val entity = createHttpEntity(bearerToken, body)

    return testRestTemplate.exchange(
      "/api/bookings/{bookingId}/move-to-cell-swap",
      PUT,
      entity,
      object : ParameterizedTypeReference<String>() {
      },
      bookingId,
    )
  }

  // Type on ParameterizedTypeReference required to work around https://bugs.openjdk.java.net/browse/JDK-8210197
  private fun requestMoveToCell(bearerToken: String, bookingId: String = BOOKING_ID_S, livingUnitId: String = INITIAL_CELL_DESC, reasonCode: String = INITIAL_REASON, dateTime: String): ResponseEntity<String> {
    val entity = createHttpEntity(bearerToken, null)
    return testRestTemplate.exchange(
      String.format("/api/bookings/%s/living-unit/%s?reasonCode=%s&dateTime=%s", bookingId, livingUnitId, reasonCode, dateTime),
      PUT,
      entity,
      object : ParameterizedTypeReference<String>() {
      },
    )
  }

  private fun verifySuccessResponse(response: ResponseEntity<String>, bookingId: Long = BOOKING_ID, internalLocationId: Long = NEW_CELL, internalLocationDesc: String = NEW_CELL_DESC) {
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(getBodyAsJsonContent<Any>(response)).extractingJsonPathNumberValue("$.bookingId").isEqualTo(bookingId.toInt())
    assertThat(getBodyAsJsonContent<Any>(response)).extractingJsonPathNumberValue("$.assignedLivingUnitId").isEqualTo(internalLocationId.toInt())
    assertThat(getBodyAsJsonContent<Any>(response)).extractingJsonPathStringValue("$.assignedLivingUnitDesc").isEqualTo(internalLocationDesc)
    assertThat(getBodyAsJsonContent<Any>(response)).extractingJsonPathNumberValue("$.bedAssignmentHistorySequence")
      .satisfies(ThrowingConsumer { number: Number -> assertThat(number.toInt()).isNotZero() })
  }

  private fun verifyErrorResponse(response: ResponseEntity<String>, status: HttpStatus, vararg partialMessages: String) {
    assertThat(response.statusCode).isEqualTo(status)
    assertThat(getBodyAsJsonContent<Any>(response)).extractingJsonPathNumberValue("$.status").isEqualTo(status.value())
    if (partialMessages[0].isNotEmpty()) {
      stream(partialMessages).forEach { partialMessage: String -> assertThat(getBodyAsJsonContent<Any>(response)).extractingJsonPathStringValue("$.userMessage").contains(partialMessage) }
    }
  }

  private fun verifyOffenderBookingLivingUnit(bookingId: Long = BOOKING_ID, livingUnitId: Long = INITIAL_CELL) {
    val offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow()
    assertThat(offenderBooking.assignedLivingUnit.locationId).isEqualTo(livingUnitId)
  }

  private fun verifyLastBedAssignmentHistory(bookingId: Long = BOOKING_ID, livingUnitId: Long = NEW_CELL, reason: String, dateTime: LocalDateTime) {
    val bedAssignmentHistories = bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId)
    assertThat(bedAssignmentHistories[bedAssignmentHistories.size - 1])
      .extracting("offenderBooking.bookingId", "livingUnitId", "assignmentReason", "assignmentDate", "assignmentDateTime")
      .containsExactlyInAnyOrder(bookingId, livingUnitId, reason, dateTime.toLocalDate(), dateTime.withNano(0))
  }

  private fun verifyLastBedAssignmentHistory(bookingId: Long = BOOKING_ID, livingUnitId: Long = INITIAL_CELL) {
    val bedAssignmentHistories = bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId)
    assertThat(bedAssignmentHistories[bedAssignmentHistories.size - 1])
      .extracting("offenderBooking.bookingId", "livingUnitId")
      .containsExactlyInAnyOrder(bookingId, livingUnitId)
  }

  companion object {
    private const val BOOKING_ID = -33L
    private const val BOOKING_ID_S = "-33"

    private const val INITIAL_CELL_DESC = "LEI-H-1-1"
    private const val INITIAL_REASON = "ADM"
    private val INITIAL_DATE_TIME: LocalDateTime = LocalDateTime.of(2020, 4, 3, 11, 0, 0)

    private const val INITIAL_CELL = -15L
    private const val NEW_CELL = 14538L
    private const val NEW_CELL_DESC = "LEI-CSWAP"
  }
}
