package net.syscon.elite.api.resource.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

public class OffenderMovementResourceImplApiTest_moveToCell extends ResourceTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void validRequest() {
        final var response = testRestTemplate.exchange("/api/bookings/1/living-unit/2?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.bookingId").isEqualTo(1);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.assignedLivingUnitId").isEqualTo(2);
    }

    @Test
    public void bookingId_invalid_badRequest() {
        final var response = testRestTemplate.exchange("/api/bookings/bad_booking_id/living-unit/1?reasonCode=ADM&dateTime=2020-03-24T13:24:35", PUT, anEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(400);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains("bad_booking_id");
    }

    private HttpEntity<?> anEntity() {
        return createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());
    }

}
