@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import oracle.jdbc.OracleDatabaseException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowingConsumer
import org.hibernate.exception.GenericJDBCException
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.hmpps.prison.api.model.CellMoveResult
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.MovementUpdateService
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain
import java.sql.SQLException
import java.time.LocalDateTime

class BookingMovementsResourceIntTest_moveToCell_mockService : ResourceTest() {
  @MockitoBean
  private lateinit var movementUpdateService: MovementUpdateService

  @Test
  fun validRequest() {
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(false),
      ),
    )
      .thenReturn(aCellMoveResult(2))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.bookingId").isEqualTo(-1)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.assignedLivingUnitId").isEqualTo(2)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.assignedLivingUnitDesc").isEqualTo("LEI-A-1-1")
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.bedAssignmentHistorySequence")
      .satisfies(ThrowingConsumer { number: Number -> assertThat(number.toInt()).isNotZero() })
  }

  @Test
  fun validRequestWithTimeoutSuccess() {
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(true),
      ),
    )
      .thenReturn(aCellMoveResult(2))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35&lockTimeout=true",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.bookingId").isEqualTo(-1)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.assignedLivingUnitId").isEqualTo(2)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.assignedLivingUnitDesc").isEqualTo("LEI-A-1-1")
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.bedAssignmentHistorySequence")
      .satisfies(ThrowingConsumer { number: Number -> assertThat(number.toInt()).isNotZero() })
  }

  @Test
  fun validRequestWithTimeoutFailure() {
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(true),
      ),
    )
      .thenThrow(DatabaseRowLockedException("developer message"))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35&lockTimeout=true",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.LOCKED)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").isEqualTo("Resource locked, possibly in use in P-Nomis.")
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.developerMessage").isEqualTo("developer message")
  }

  @Test
  fun validRequest_passesParametersToService() {
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(false),
      ),
    )
      .thenReturn(aCellMoveResult(1))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.OK)
    Mockito.verify<MovementUpdateService>(movementUpdateService)
      .moveToCellOrReception(-1L, "LEI-A-1-1", "ADM", LocalDateTime.of(2020, 3, 24, 13, 24, 35), false)
  }

  @Test
  fun validRequest_missingOptionalDateTimeValue_isOk() {
    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.OK)
  }

  @Test
  fun validRequest_missingOptionalDateTime_isOk() {
    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=ADM",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.OK)
  }

  @Test
  fun bookingId_invalid_badRequest() {
    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/invalid_booking_id/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(400)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").contains("invalid_booking_id")
  }

  @Test
  fun bookingId_notFound() {
    val bookingNotFoundError = "Offender booking with id 123 not found."
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(false),
      ),
    )
      .thenThrow(EntityNotFoundException.withMessage(bookingNotFoundError))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/123/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(404)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").isEqualTo(bookingNotFoundError)
  }

  @Test
  fun livingUnitId_notFound() {
    val livingUnitNotFoundError = "Living unit with id Z-1 not found."
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(false),
      ),
    )
      .thenThrow(EntityNotFoundException.withMessage(livingUnitNotFoundError))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/Z-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(404)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").isEqualTo(livingUnitNotFoundError)
  }

  @Test
  fun livingUnitId_inactive_badRequest() {
    val livingUnitNotFoundError = "Living unit Z-1 not found."
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(false),
      ),
    )
      .thenThrow(IllegalArgumentException(livingUnitNotFoundError))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/Z-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(400)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").isEqualTo(livingUnitNotFoundError)
  }

  @Test
  fun reasonCode_notFound_badRequest() {
    val reasonCodeNotFoundError = "Reference code for domain [${ReferenceDomain.CELL_MOVE_REASON.domain}] and code [LEI-A-1-1] not found."
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(false),
      ),
    )
      .thenThrow(
        IllegalArgumentException(
          reasonCodeNotFoundError,
          EntityNotFoundException.withMessage(reasonCodeNotFoundError),
        ),
      )

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=123&dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(400)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").isEqualTo(reasonCodeNotFoundError)
  }

  @Test
  fun reasonCode_missingValue_badRequest() {
    val reasonCodeMissingError = "Reason code is mandatory"
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(false),
      ),
    )
      .thenThrow(IllegalArgumentException(reasonCodeMissingError))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=&dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(400)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").isEqualTo(reasonCodeMissingError)
  }

  @Test
  fun reasonCode_missing_badRequest() {
    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(400)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").contains("reasonCode")
  }

  @Test
  fun dateTime_invalid_badRequest() {
    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=invalid_date",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(400)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").contains("invalid_date")
  }

  @Test
  fun dateTime_inTheFuture_badRequest() {
    val dateTimeError = "The date cannot be in the future"
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(false),
      ),
    )
      .thenThrow(IllegalArgumentException(dateTimeError))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=3020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(400)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").isEqualTo(dateTimeError)
  }

  @Test
  fun server_error() {
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(false),
      ),
    )
      .thenThrow(RuntimeException("Some exception"))

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(500)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage").isEqualTo("Some exception")
  }

  @Test
  fun sql_error() {
    whenever<CellMoveResult>(
      movementUpdateService.moveToCellOrReception(
        anyLong(),
        anyString(),
        anyString(),
        any<LocalDateTime>(
          LocalDateTime::class.java,
        ),
        eq(false),
      ),
    )
      .thenThrow(capacityReachedException())

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/bookings/-1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35",
      HttpMethod.PUT,
      anEntity(),
      String::class.java,
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("$.status").isEqualTo(400)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.userMessage")
      .isEqualTo("Error: There is no more capacity in this location.")
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathStringValue("$.developerMessage")
      .contains("ORA-20959: Error: There is no more capacity in this location.")
      .contains("ORA-04088: error during execution of trigger 'OMS_OWNER.OFFENDER_BOOKINGS_T2")
  }

  private fun anEntity(): HttpEntity<*> = createHttpEntityWithBearerAuthorisation("ITAG_USER", mutableListOf(), emptyMap())

  private fun aCellMoveResult(bedAssignmentHistorySequence: Int): CellMoveResult = CellMoveResult.builder()
    .bookingId(-1L)
    .assignedLivingUnitId(2L)
    .assignedLivingUnitDesc("LEI-A-1-1")
    .bedAssignmentHistorySequence(bedAssignmentHistorySequence)
    .build()

  private fun capacityReachedException(): JpaSystemException {
    val sqlException = SQLException(
      """
                ORA-20959: Error: There is no more capacity in this location.
                ORA-06512: at "OMS_OWNER.TAG_ESTABLISHMENT", line 200
                ORA-06512: at "OMS_OWNER.OFFENDER_BOOKINGS_T2", line 38
                ORA-06512: at "OMS_OWNER.TAG_ERROR", line 169
                ORA-06512: at "OMS_OWNER.OFFENDER_BOOKINGS_T2", line 44
                ORA-04088: error during execution of trigger 'OMS_OWNER.OFFENDER_BOOKINGS_T2'
                
      """.trimIndent(),
      OracleDatabaseException(0, 20959, "some detail message", "some sql", "some original sql"),
    )
    val genericJDBCException = GenericJDBCException("could not execute statement", sqlException)
    return JpaSystemException(genericJDBCException)
  }
}
