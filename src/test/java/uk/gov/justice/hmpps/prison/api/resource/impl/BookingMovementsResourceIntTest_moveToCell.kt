@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.service.BedAssignmentHistoryService
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@ContextConfiguration(classes = [BookingMovementsResourceIntTest_moveToCell.TestClock::class])
class BookingMovementsResourceIntTest_moveToCell : ResourceTest() {
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

  @MockitoSpyBean
  private lateinit var bedAssignmentHistoryService: BedAssignmentHistoryService

  @AfterEach
  fun tearDown() {
    // Return the offender back to his original cell as configured in the test data in R__3_6_1__OFFENDER_BOOKINGS.sql
    requestMoveToCell(
      validToken(),
      BOOKING_ID_S,
      INITIAL_CELL_DESC,
      INITIAL_REASON,
      INITIAL_DATE_TIME.format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )
  }

  @Test
  fun validRequest() {
    val dateTime = LocalDateTime.now().minusHours(1)

    val response: ResponseEntity<String> = requestMoveToCell(
      validToken(),
      BOOKING_ID_S,
      NEW_CELL_DESC,
      "BEH",
      dateTime.format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )

    verifySuccessResponse(response, BOOKING_ID, NEW_CELL, NEW_CELL_DESC)
    verifyOffenderBookingLivingUnit(BOOKING_ID, NEW_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, NEW_CELL, "BEH", dateTime)
  }

  @Test
  fun missingDate_defaultsToNow() {
    val expectedDateTime = clock.instant().atZone(ZoneId.systemDefault()).toLocalDateTime()

    val response: ResponseEntity<String> = requestMoveToCell(validToken(), BOOKING_ID_S, NEW_CELL_DESC, "BEH", "")

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
      BOOKING_ID_S,
      NEW_CELL_DESC,
      "BEH",
      dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )
    verifyOffenderBookingLivingUnit(BOOKING_ID, NEW_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, NEW_CELL, "BEH", dateTime)

    val response: ResponseEntity<String> = requestMoveToCell(
      validToken(),
      BOOKING_ID_S,
      INITIAL_CELL_DESC,
      "CON",
      moveBackDateTime.format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )
    verifySuccessResponse(response, BOOKING_ID, INITIAL_CELL, INITIAL_CELL_DESC)
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, "CON", moveBackDateTime)
  }

  @Test
  fun noChange_notUpdated() {
    val dateTime = LocalDateTime.now().minusHours(1)

    val response: ResponseEntity<String> = requestMoveToCell(
      validToken(),
      BOOKING_ID_S,
      INITIAL_CELL_DESC,
      "BEH",
      dateTime.plusMinutes(1).format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )

    verifySuccessResponse(response, BOOKING_ID, INITIAL_CELL, INITIAL_CELL_DESC)
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, INITIAL_REASON, INITIAL_DATE_TIME)
  }

  @Test
  fun notFound() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val invalidBookingId = "-69854"

    val response: ResponseEntity<String> = requestMoveToCell(
      validToken(),
      invalidBookingId,
      NEW_CELL_DESC,
      "BEH",
      dateTime.plusMinutes(1).format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )

    verifyErrorResponse(response, HttpStatus.NOT_FOUND, invalidBookingId)
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun locationTypeNotACell_badRequest() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val wing = "LEI-A"

    val response: ResponseEntity<String> = requestMoveToCell(
      validToken(),
      BOOKING_ID_S,
      wing,
      "BEH",
      dateTime.format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )

    verifyErrorResponse(response, HttpStatus.BAD_REQUEST, wing)
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun locationFull_badRequest() {
    val dateTime = LocalDateTime.now().minusHours(1)
    val wing = "LEI-A-1-3"

    val response: ResponseEntity<String> = requestMoveToCell(
      validToken(),
      BOOKING_ID_S,
      wing,
      "BEH",
      dateTime.format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )

    verifyErrorResponse(
      response,
      HttpStatus.BAD_REQUEST,
      "Location LEI-A-1-3 is either not a cell or reception, active or is at maximum capacity",
    )
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun noBookingAccess_forbidden() {
    val dateTime = LocalDateTime.now().minusHours(1)

    val response: ResponseEntity<String> = requestMoveToCell(
      differentAgencyToken(),
      BOOKING_ID_S,
      NEW_CELL_DESC,
      "BEH",
      dateTime.plusMinutes(1).format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )

    verifyErrorResponse(response, HttpStatus.FORBIDDEN, BOOKING_ID_S)
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun userReadOnly_forbidden() {
    val dateTime = LocalDateTime.now().minusHours(1)

    val response: ResponseEntity<String> = requestMoveToCell(
      readOnlyToken(),
      BOOKING_ID_S,
      NEW_CELL_DESC,
      "BEH",
      dateTime.plusMinutes(1).format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )

    verifyErrorResponse(response, HttpStatus.FORBIDDEN, "")
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun offenderInDifferentPrison_badRequest() {
    val dateTime = LocalDateTime.now().minusHours(1)

    val response: ResponseEntity<String> = requestMoveToCell(
      validToken(),
      BOOKING_ID_S,
      CELL_DIFF_PRISON_S,
      "BEH",
      dateTime.plusMinutes(1).format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )

    verifyErrorResponse(response, HttpStatus.BAD_REQUEST, "MDI", "LEI")
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  @Test
  fun transactionRolledBack() {
    val dateTime = LocalDateTime.now().minusHours(1)

    Mockito.doThrow(RuntimeException::class.java)
      .`when`<BedAssignmentHistoryService>(bedAssignmentHistoryService).add(BOOKING_ID, NEW_CELL, "BEH", dateTime)
    val response: ResponseEntity<String> = requestMoveToCell(
      validToken(),
      BOOKING_ID_S,
      NEW_CELL_DESC,
      "BEH",
      dateTime.format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )

    verifyErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "")
    verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL)
    verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL)
  }

  private fun differentAgencyToken(): String = jwtAuthenticationHelper.createJwtAccessToken(
    "prison-api-client",
    "WAI_USER",
    mutableListOf("read", "write"),
  )

  private fun requestMoveToCell(
    bearerToken: String,
    bookingId: String,
    livingUnitId: String,
    reasonCode: String,
    dateTime: String,
  ): ResponseEntity<String> {
    val entity = createHttpEntity(bearerToken, null)
    return testRestTemplate.exchange(
      "/api/bookings/$bookingId/living-unit/$livingUnitId?reasonCode=$reasonCode&dateTime=$dateTime",
      HttpMethod.PUT,
      entity,
      object : ParameterizedTypeReference<String>() {
      },
    )
  }

  private fun verifySuccessResponse(
    response: ResponseEntity<String>,
    bookingId: Long,
    internalLocationId: Long,
    internalLocationDesc: String,
  ) {
    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.bookingId").isEqualTo(bookingId.toInt())
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.assignedLivingUnitId").isEqualTo(internalLocationId.toInt())
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.assignedLivingUnitDesc").isEqualTo(internalLocationDesc)
  }

  private fun verifyErrorResponse(
    response: ResponseEntity<String>,
    status: HttpStatus,
    vararg partialMessages: String,
  ) {
    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(status)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(status.value())
    if (!partialMessages[0].isEmpty()) {
      partialMessages.forEach { partialMessage: String ->
        assertThat(
          getBodyAsJsonContent<Any>(
            response,
          ),
        ).extractingJsonPathStringValue("$.userMessage").contains(partialMessage)
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
    assertThat<BedAssignmentHistory>(bedAssignmentHistories.get(bedAssignmentHistories.size - 1))
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
    assertThat<BedAssignmentHistory>(bedAssignmentHistories.get(bedAssignmentHistories.size - 1))
      .extracting("offenderBooking.bookingId", "livingUnitId")
      .containsExactlyInAnyOrder(bookingId, livingUnitId)
  }

  companion object {
    private val BOOKING_ID = -33L
    private const val BOOKING_ID_S = "-33"

    private val INITIAL_CELL = -15L
    private const val INITIAL_CELL_DESC = "LEI-H-1-1"
    private const val INITIAL_REASON = "ADM"
    private val INITIAL_DATE_TIME: LocalDateTime = LocalDateTime.of(2020, 4, 3, 11, 0, 0)

    private val NEW_CELL = -18L
    private const val NEW_CELL_DESC = "LEI-H-1-4"
    private const val CELL_DIFF_PRISON_S = "MDI-1-1-001"
  }
}
