@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.service.BedAssignmentHistoryService
import uk.gov.justice.hmpps.prison.util.JwtParameters
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

@ContextConfiguration(classes = [OffenderMovementsResourceIntTest_moveToCell.TestClock::class])
class OffenderMovementsResourceIntTest_moveToCell : ResourceTest() {
  @TestConfiguration
  internal class TestClock {
    @Bean
    fun clock(): Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
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
    requestMoveToCell(
      validToken(),
      OFFENDER_NO,
      INITIAL_CELL_DESC,
      INITIAL_REASON,
      INITIAL_DATE_TIME.format(ISO_LOCAL_DATE_TIME),
    )
  }

  @Test
  fun validRequest() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val response = requestMoveToCell(
      validToken(),
      OFFENDER_NO,
      NEW_CELL_DESC,
      "BEH",
      dateTime.format(ISO_LOCAL_DATE_TIME),
    )
    verifySuccessResponse(response, BOOKING_ID, NEW_CELL, NEW_CELL_DESC)
    verifyOffenderBookingLivingUnit(BOOKING_ID, NEW_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, NEW_CELL, "BEH", dateTime)
  }

  @Test
  fun missingDate_defaultsToNow() {
    val expectedDateTime = clock.instant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    val response = requestMoveToCell(validToken(), OFFENDER_NO, NEW_CELL_DESC, "BEH", "")
    verifySuccessResponse(response, BOOKING_ID, NEW_CELL, NEW_CELL_DESC)
    verifyOffenderBookingLivingUnit(BOOKING_ID, NEW_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, NEW_CELL, "BEH", expectedDateTime)
  }

  @Test
  fun backToOriginalCell() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val moveBackDateTime = LocalDateTime.now().minusMinutes(1)
    requestMoveToCell(
      validToken(),
      OFFENDER_NO,
      NEW_CELL_DESC,
      "BEH",
      dateTime.format(ISO_LOCAL_DATE_TIME),
    )
    verifyOffenderBookingLivingUnit(BOOKING_ID, NEW_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, NEW_CELL, "BEH", dateTime)
    val response = requestMoveToCell(
      validToken(),
      OFFENDER_NO,
      INITIAL_CELL_DESC,
      "CON",
      moveBackDateTime.format(ISO_LOCAL_DATE_TIME),
    )
    verifySuccessResponse(response, BOOKING_ID, INITIAL_CELL, INITIAL_CELL_DESC)
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, "CON", moveBackDateTime)
  }

  @Test
  fun noChange_notUpdated() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val response = requestMoveToCell(
      validToken(),
      OFFENDER_NO,
      INITIAL_CELL_DESC,
      "BEH",
      dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME),
    )
    verifySuccessResponse(response, BOOKING_ID, INITIAL_CELL, INITIAL_CELL_DESC)
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, INITIAL_REASON, INITIAL_DATE_TIME)
  }

  @Test
  fun `ensure unilink can access with client token`() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val response = requestMoveToCell(
      createJwt("unilink", listOf("ROLE_UNILINK")),
      OFFENDER_NO,
      NEW_CELL_DESC,
      "BEH",
      dateTime.format(ISO_LOCAL_DATE_TIME),
    )
    verifySuccessResponse(response, BOOKING_ID, NEW_CELL, NEW_CELL_DESC)
  }

  @Test
  fun notFound() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val invalidBookingId = "-69854"
    val response = requestMoveToCell(
      validToken(),
      invalidBookingId,
      NEW_CELL_DESC,
      "BEH",
      dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME),
    )
    verifyErrorResponse(response, HttpStatus.NOT_FOUND, invalidBookingId)
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun locationTypeNotACell_badRequest() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val wing = "LEI-A"
    val response = requestMoveToCell(
      validToken(),
      OFFENDER_NO,
      wing,
      "BEH",
      dateTime.format(ISO_LOCAL_DATE_TIME),
    )
    verifyErrorResponse(response, HttpStatus.BAD_REQUEST, wing)
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun locationFull_badRequest() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val wing = "LEI-A-1-3"
    val response = requestMoveToCell(
      validToken(),
      OFFENDER_NO,
      wing,
      "BEH",
      dateTime.format(ISO_LOCAL_DATE_TIME),
    )
    verifyErrorResponse(
      response,
      HttpStatus.BAD_REQUEST,
      "Location LEI-A-1-3 is either not a cell, active or is at maximum capacity",
    )
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun noBookingAccess_notFound() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val response = requestMoveToCell(
      differentAgencyToken(),
      OFFENDER_NO,
      NEW_CELL_DESC,
      "BEH",
      dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME),
    )
    verifyErrorResponse(response, HttpStatus.NOT_FOUND, BOOKING_ID.toString())
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun userReadOnly_forbidden() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val response = requestMoveToCell(
      readOnlyToken(),
      OFFENDER_NO,
      NEW_CELL_DESC,
      "BEH",
      dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME),
    )
    verifyErrorResponse(response, HttpStatus.FORBIDDEN, "")
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun offenderInDifferentPrison_badRequest() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val response = requestMoveToCell(
      validToken(),
      OFFENDER_NO,
      CELL_DIFF_PRISON_S,
      "BEH",
      dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME),
    )
    verifyErrorResponse(response, HttpStatus.BAD_REQUEST, "MDI", "LEI")
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  @WithMockUser(
    username = "ITAG_USER",
    authorities = ["SCOPE_write"],
  ) // Required because stubbing the BedAssignmentHistoryService means we don't pick up the usual Authentication from Spring AOP.
  fun transactionRolledBack() {
    val dateTime = LocalDateTime.now().minusHours(1)
    Mockito.doThrow(RuntimeException::class.java).`when`(bedAssignmentHistoryService)
      .add(BOOKING_ID, NEW_CELL, "BEH", dateTime)
    val response = requestMoveToCell(
      validToken(),
      OFFENDER_NO,
      NEW_CELL_DESC,
      "BEH",
      dateTime.format(ISO_LOCAL_DATE_TIME),
    )
    verifyErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "")
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  private fun differentAgencyToken(): String = jwtAuthenticationHelper.createJwt(
    JwtParameters.builder()
      .username("WAI_USER")
      .scope(listOf("read", "write"))
      .roles(listOf())
      .expiryTime(Duration.ofDays((365 * 10).toLong()))
      .build(),
  )

  private fun requestMoveToCell(
    bearerToken: String,
    offenderNo: String,
    livingUnitId: String,
    reasonCode: String,
    dateTime: String,
  ): ResponseEntity<String?>? {
    val entity = createHttpEntity(bearerToken, null)
    return testRestTemplate.exchange(
      "/api/offenders/$offenderNo/living-unit/$livingUnitId?reasonCode=$reasonCode&dateTime=$dateTime",
      HttpMethod.PUT,
      entity,
      object : ParameterizedTypeReference<String?>() {},
    )
  }

  private fun verifySuccessResponse(
    response: ResponseEntity<String?>?,
    bookingId: Long,
    internalLocationId: Long,
    internalLocationDesc: String,
  ) {
    assertThat(response!!.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(getBodyAsJsonContent<Any?>(response)).extractingJsonPathNumberValue("$.bookingId").isEqualTo(
      bookingId.toInt(),
    )
    assertThat(getBodyAsJsonContent<Any?>(response)).extractingJsonPathNumberValue("$.assignedLivingUnitId")
      .isEqualTo(
        internalLocationId.toInt(),
      )
    assertThat(getBodyAsJsonContent<Any?>(response))
      .extractingJsonPathStringValue("$.assignedLivingUnitDesc").isEqualTo(internalLocationDesc)
  }

  private fun verifyErrorResponse(
    response: ResponseEntity<String?>?,
    status: HttpStatus?,
    vararg partialMessages: String?,
  ) {
    assertThat(response!!.statusCode).isEqualTo(status)
    assertThat(getBodyAsJsonContent<Any?>(response)).extractingJsonPathNumberValue("$.status").isEqualTo(
      status?.value(),
    )
    if (partialMessages[0]!!.isNotEmpty()) {
      partialMessages.forEach { partialMessage: String? ->
        assertThat(getBodyAsJsonContent<Any?>(response)).extractingJsonPathStringValue("$.userMessage")
          .contains(partialMessage)
      }
    }
  }

  private fun verifyOffenderBookingLivingUnit(bookingId: Long, livingUnitId: Long) {
    val offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow()
    assertThat(offenderBooking.assignedLivingUnit.locationId).isEqualTo(livingUnitId)
  }

  private fun verifyLastBedAssignmentHistory(
    bookingId: Long,
    livingUnitId: Long,
    reason: String,
    dateTime: LocalDateTime,
  ) {
    val bedAssignmentHistories =
      bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId)
    assertThat(
      bedAssignmentHistories[bedAssignmentHistories.size - 1],
    )
      .extracting(
        "offenderBooking.bookingId",
        "livingUnitId",
        "assignmentReason",
        "assignmentDate",
        "assignmentDateTime",
      )
      .containsExactlyInAnyOrder(bookingId, livingUnitId, reason, dateTime.toLocalDate(), dateTime.withNano(0))
  }

  private fun verifyLastBedAssignmentHistory(bookingId: Long, livingUnitId: Long) {
    val bedAssignmentHistories =
      bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId)
    assertThat(
      bedAssignmentHistories[bedAssignmentHistories.size - 1],
    )
      .extracting("offenderBooking.bookingId", "livingUnitId")
      .containsExactlyInAnyOrder(bookingId, livingUnitId)
  }

  companion object {
    private const val BOOKING_ID: Long = -33L
    private const val OFFENDER_NO: String = "A5577RS"
    private const val INITIAL_CELL: Long = -15L
    private const val INITIAL_CELL_DESC: String = "LEI-H-1-1"
    private const val INITIAL_REASON: String = "ADM"
    private val INITIAL_DATE_TIME = LocalDateTime.of(2020, 4, 3, 11, 0, 0)
    private const val NEW_CELL: Long = -18L
    private const val NEW_CELL_DESC: String = "LEI-H-1-4"
    private const val CELL_DIFF_PRISON_S: String = "MDI-1-1-001"
  }
}
