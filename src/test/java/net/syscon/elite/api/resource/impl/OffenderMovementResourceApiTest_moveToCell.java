package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.service.MovementUpdateService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

// TODO DT-235 These tests are here to help define the API, but currently using canned data.  Once MovementUpdateService and dependencies are implemented, replace below with real data.
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
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.internalLocationId").isEqualTo("2");
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
    public void validRequest_missingOptionalDateTime_defaultsToToday() {
        when(movementUpdateService.moveToCell(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
                .thenReturn(anOffenderSummary(1L, 2L));

        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=ADM", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        ArgumentCaptor<LocalDateTime> dateTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(movementUpdateService).moveToCell(anyLong(), anyLong(), anyString(), dateTimeCaptor.capture());
        assertThat(dateTimeCaptor.getValue().toLocalDate()).isEqualTo(LocalDate.now());
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
        final var response = testRestTemplate.exchange("/api/bookings/123/living-unit/1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("123");
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
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/123?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("123");
    }

    @Test
    public void livingUnitId_missing_notFound() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
    }

    @Test
    public void reasonCode_notFound() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=123&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(404);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("123");
    }

    @Test
    public void reasonCode_missingValue_badRequest() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").containsIgnoringCase("reason code");
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
    public void server_error() {
        final var response = testRestTemplate.exchange("/api/bookings/456/living-unit/2?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(500);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("server error");
    }

    private HttpEntity<?> anEntity() {
        return createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
    }

    private OffenderSummary anOffenderSummary(final Long bookingId, final Long livingUnitId) {
        return OffenderSummary.builder()
                .bookingId(bookingId)
                .internalLocationId(String.valueOf(livingUnitId))
                .build();

    }

}
