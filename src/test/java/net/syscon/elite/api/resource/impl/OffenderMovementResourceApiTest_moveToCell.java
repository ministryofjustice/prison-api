package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.MovementUpdateService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static net.syscon.elite.service.support.ReferenceDomain.CELL_MOVE_REASON;
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

public class OffenderMovementResourceApiTest_moveToCell extends ResourceTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private MovementUpdateService movementUpdateService;

    @Test
    public void validRequest() {
        when(movementUpdateService.moveToCell(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
                .thenReturn(anOffenderSummary(1L, 2L));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.bookingId").isEqualTo(1);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.assignedLivingUnitId").isEqualTo(2);
    }

    @Test
    public void validRequest_passesParametersToService() {
        when(movementUpdateService.moveToCell(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
                .thenReturn(anOffenderSummary(1L, 2L));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        verify(movementUpdateService).moveToCell(1L, 2L, "ADM", LocalDateTime.of(2020, 3, 24, 13, 24, 35));
    }

    @Test
    public void validRequest_missingOptionalDateTimeValue_isOk() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=ADM&dateTime=", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void validRequest_missingOptionalDateTime_isOk() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=ADM", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void bookingId_invalid_badRequest() {
        final var response = testRestTemplate.exchange("/api/bookings/invalid_booking_id/living-unit/1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("invalid_booking_id");
    }

    @Test
    public void bookingId_notFound() {
        final var bookingNotFoundError = "Booking id 123 not found.";
        when(movementUpdateService.moveToCell(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
                .thenThrow(EntityNotFoundException.withMessage(bookingNotFoundError));

        final var response = testRestTemplate.exchange("/api/bookings/123/living-unit/1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(bookingNotFoundError);
    }

    @Test
    public void bookingId_missing_notFound() {
        final var response = testRestTemplate.exchange("/api/bookings//living-unit/1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
    }

    @Test
    public void livingUnitId_invalid_badRequest() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/invalid_living_unit_id?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("invalid_living_unit_id");
    }

    @Test
    public void livingUnitId_notFound() {
        final var livingUnitNotFoundError = "Living unit with id 123 not found.";
        when(movementUpdateService.moveToCell(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
                .thenThrow(EntityNotFoundException.withMessage(livingUnitNotFoundError));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/123?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(livingUnitNotFoundError);
    }

    @Test
    public void livingUnitId_missing_notFound() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
    }

    @Test
    public void reasonCode_notFound() {
        final var reasonCodeNotFoundError = format("Reference code for domain [%s] and code [123] not found.", CELL_MOVE_REASON.getDomain());
        when(movementUpdateService.moveToCell(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
                .thenThrow(EntityNotFoundException.withMessage(reasonCodeNotFoundError));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=123&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(reasonCodeNotFoundError);
    }

    @Test
    public void reasonCode_missingValue_badRequest() {
        final var reasonCodeMissingError = "Reason code is mandatory";
        when(movementUpdateService.moveToCell(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException(reasonCodeMissingError));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(reasonCodeMissingError);
    }

    @Test
    public void reasonCode_missing_badRequest() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("reasonCode");
    }

    @Test
    public void dateTime_invalid_badRequest() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=ADM&dateTime=invalid_date", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("invalid_date");
    }

    @Test
    public void dateTime_inTheFuture_badRequest() {
        final var dateTimeError = "The date cannot be in the future";
        when(movementUpdateService.moveToCell(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException(dateTimeError));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=ADM&dateTime=3020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo(dateTimeError);
    }

    @Test
    public void server_error() {
        when(movementUpdateService.moveToCell(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Some exception"));

        final var response = testRestTemplate.exchange("/api/bookings/456/living-unit/2?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(500);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").isEqualTo("Some exception");
    }

    private HttpEntity<?> anEntity() {
        return createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
    }

    private OffenderBooking anOffenderSummary(final Long bookingId, final Long livingUnitId) {
        return OffenderBooking.builder()
                .bookingId(bookingId)
                .assignedLivingUnitId(livingUnitId)
                .build();

    }

}
