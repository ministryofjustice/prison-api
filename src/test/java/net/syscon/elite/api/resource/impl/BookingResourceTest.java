package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.BookingActivity;
import net.syscon.elite.api.model.CreateAlert;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.UpdateAttendanceBatch;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingResourceTest extends ResourceTest {

    @Autowired
    private AuthTokenHelper authTokenHelper;

    @Test
    public void testThatUpdateAttendanceIsLockedDown_WhenPayRoleIsMissing() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var body = Map.of("eventOutcome", "ATT", "performance", "STANDARD");
        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/activities/{activityId}/attendance",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {},
                -2, -11);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testUpdateAttendance_WithTheValidRole() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.PAY);

        final var body = Map.of("eventOutcome", "ATT", "performance", "STANDARD");
        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/activities/{activityId}/attendance",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                -2, -11);

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void testUpdateAttendance_WithMultipleBookingIds() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.PAY);

        final var body = UpdateAttendanceBatch
                .builder()
                .eventOutcome("ATT")
                .performance("STANDARD")
                .bookingActivities(Set.of(BookingActivity.builder().activityId(-11L).bookingId(-2L).build()))
                .build();

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
                "/api/bookings/activities/attendance",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {});

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void testCreateNewAlert_UnAuthorised() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var body = CreateAlert.builder().alertCode("X").alertType("XX").comment("XXX")
                .alertDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert",
                HttpMethod.POST,
                createHttpEntity(token , body),
                new ParameterizedTypeReference<ErrorResponse>() {}, -10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testCreateNewAlert_BadRequest() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var body = CreateAlert.builder().build();

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert",
                HttpMethod.POST,
                createHttpEntity(token , body),
                new ParameterizedTypeReference<ErrorResponse>() {}, -10L);

        final var validationMessages = response.getBody().getUserMessage();

        assertThat(validationMessages).contains("alertType");
        assertThat(validationMessages).contains("alertCode");
        assertThat(validationMessages).contains("comment");
        assertThat(validationMessages).contains("alertDate");
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }


    @Test
    public void testCreateNewAlert() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.UPDATE_ALERT);

        final var body = CreateAlert.builder().alertCode("X").alertType("XX").comment("Attacked staff")
                .alertDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert",
                HttpMethod.POST,
                createHttpEntity(token , body),
                new ParameterizedTypeReference<Long>() {}, -10L);

        assertThat(response.getBody()).isGreaterThan(1);
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }
}
