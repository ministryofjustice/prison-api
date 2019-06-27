package net.syscon.elite.api.resource.impl;

import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Map;

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
}
