package uk.gov.justice.hmpps.prison.api.resource.impl;

import oracle.jdbc.OracleDatabaseException;
import org.hibernate.exception.GenericJDBCException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.orm.jpa.JpaSystemException;
import uk.gov.justice.hmpps.prison.api.model.CellMoveResult;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.MovementUpdateService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.justice.hmpps.prison.service.support.ReferenceDomain.CELL_MOVE_REASON;

public class OffenderMovementResourceApiTest_moveToCell extends ResourceTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private MovementUpdateService movementUpdateService;

    @Test
    public void validRequest() {
        when(movementUpdateService.moveToCell(anyLong(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenReturn(aCellMoveResult(1L, 2L, "LEI-A-1-1", 2));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.bookingId").isEqualTo(1);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.assignedLivingUnitId").isEqualTo(2);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.assignedLivingUnitDesc").isEqualTo("LEI-A-1-1");
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.bedAssignmentHistorySequence")
                .satisfies((number) -> assertThat(number.intValue()).isNotZero());
    }

    @Test
    public void validRequest_passesParametersToService() {
        when(movementUpdateService.moveToCell(anyLong(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenReturn(aCellMoveResult(1L, 2L, "LEI-A-1-1", 1));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        verify(movementUpdateService).moveToCell(1L, "LEI-A-1-1", "ADM", LocalDateTime.of(2020, 3, 24, 13, 24, 35));
    }

    @Test
    public void validRequest_missingOptionalDateTimeValue_isOk() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void validRequest_missingOptionalDateTime_isOk() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/LEI-A-1-1?reasonCode=ADM", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void bookingId_invalid_badRequest() {
        final var response = testRestTemplate.exchange("/api/bookings/invalid_booking_id/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("invalid_booking_id");
    }

    @Test
    public void bookingId_notFound() {
        final var bookingNotFoundError = "Booking id 123 not found.";
        when(movementUpdateService.moveToCell(anyLong(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenThrow(EntityNotFoundException.withMessage(bookingNotFoundError));

        final var response = testRestTemplate.exchange("/api/bookings/123/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(bookingNotFoundError);
    }

    @Test
    public void livingUnitId_notFound() {
        final var livingUnitNotFoundError = "Living unit with id Z-1 not found.";
        when(movementUpdateService.moveToCell(anyLong(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenThrow(EntityNotFoundException.withMessage(livingUnitNotFoundError));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/Z-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(livingUnitNotFoundError);
    }

    @Test
    public void livingUnitId_inactive_badRequest() {
        final var livingUnitNotFoundError = "Living unit Z-1 not found.";
        when(movementUpdateService.moveToCell(anyLong(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException(livingUnitNotFoundError));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/Z-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(livingUnitNotFoundError);
    }

    @Test
    public void reasonCode_notFound_badRequest() {
        final var reasonCodeNotFoundError = format("Reference code for domain [%s] and code [LEI-A-1-1] not found.", CELL_MOVE_REASON.getDomain());
        when(movementUpdateService.moveToCell(anyLong(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException(reasonCodeNotFoundError, EntityNotFoundException.withMessage(reasonCodeNotFoundError)));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/LEI-A-1-1?reasonCode=123&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(reasonCodeNotFoundError);
    }

    @Test
    public void reasonCode_missingValue_badRequest() {
        final var reasonCodeMissingError = "Reason code is mandatory";
        when(movementUpdateService.moveToCell(anyLong(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException(reasonCodeMissingError));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/LEI-A-1-1?reasonCode=&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(reasonCodeMissingError);
    }

    @Test
    public void reasonCode_missing_badRequest() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/LEI-A-1-1?dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("reasonCode");
    }

    @Test
    public void dateTime_invalid_badRequest() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=invalid_date", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("invalid_date");
    }

    @Test
    public void dateTime_inTheFuture_badRequest() {
        final var dateTimeError = "The date cannot be in the future";
        when(movementUpdateService.moveToCell(anyLong(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException(dateTimeError));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=3020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(dateTimeError);
    }

    @Test
    public void server_error() {
        when(movementUpdateService.moveToCell(anyLong(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Some exception"));

        final var response = testRestTemplate.exchange("/api/bookings/456/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(500);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo("Some exception");
    }

    @Test
    public void sql_error() {
        when(movementUpdateService.moveToCell(anyLong(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenThrow(capacityReachedException());

        final var response = testRestTemplate.exchange("/api/bookings/456/living-unit/LEI-A-1-1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage")
                .isEqualTo("Error: There is no more capacity in this location.");
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.developerMessage")
                .contains("ORA-20959: Error: There is no more capacity in this location.")
                .contains("ORA-04088: error during execution of trigger 'OMS_OWNER.OFFENDER_BOOKINGS_T2");
    }

    private HttpEntity<?> anEntity() {
        return createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
    }

    private CellMoveResult aCellMoveResult(final Long bookingId, final Long livingUnitId, final String livingUnitDesc, final Integer bedAssignmentHistorySequence) {
        return CellMoveResult.builder()
                .bookingId(bookingId)
                .assignedLivingUnitId(livingUnitId)
                .assignedLivingUnitDesc(livingUnitDesc)
                .bedAssignmentHistorySequence(bedAssignmentHistorySequence)
                .build();
    }

    private JpaSystemException capacityReachedException() {
        final var sqlException = new SQLException("ORA-20959: Error: There is no more capacity in this location.\n" +
                "ORA-06512: at \"OMS_OWNER.TAG_ESTABLISHMENT\", line 200\n" +
                "ORA-06512: at \"OMS_OWNER.OFFENDER_BOOKINGS_T2\", line 38\n" +
                "ORA-06512: at \"OMS_OWNER.TAG_ERROR\", line 169\n" +
                "ORA-06512: at \"OMS_OWNER.OFFENDER_BOOKINGS_T2\", line 44\n" +
                "ORA-04088: error during execution of trigger 'OMS_OWNER.OFFENDER_BOOKINGS_T2'\n", new OracleDatabaseException(0, 20959, "some detail message", "some sql", "some original sql"));
        final var genericJDBCException = new GenericJDBCException("could not execute statement", sqlException);
        return new JpaSystemException(genericJDBCException);
    }

}
