package net.syscon.elite.api.resource.impl;

import net.syscon.elite.util.JwtParameters;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingResourceTest extends ResourceTest {

    @Test
    public void testThatUpdateAttendanceIsLockedDown_WhenPayRoleIsMissing() {

        final var token = jwtAuthenticationHelper.createJwt(JwtParameters
                .builder()
                .expiryTime(Duration.ofDays(1))
                .username("ITAG_USER")
                .scope(List.of("read", "write"))
                .build());

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
        final var token = jwtAuthenticationHelper.createJwt(JwtParameters
                .builder()
                .expiryTime(Duration.ofDays(1))
                .username("ITAG_USER")
                .scope(List.of("read", "write"))
                .roles(List.of("ROLE_PAY"))
                .build());

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
