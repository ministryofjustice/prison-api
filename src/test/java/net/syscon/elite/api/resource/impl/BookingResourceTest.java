package net.syscon.elite.api.resource.impl;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingResourceTest extends ResourceTest {

    @Test
    public void testThatUpdateAttendanceIsLockedDown_WhenPayRoleIsMissing() {
        final var headers = new HttpHeaders();

        final var token = jwtAuthenticationHelper.createJwt("staff1", Duration.ofDays(1), List.of("read", "write"));
        final var body = Map.of("eventOutcome", "ATT", "performance", "STANDARD");

        headers.add("Authorization", "Bearer " + token);

        final var httpEntity = new HttpEntity(body, headers);

        final var response = testRestTemplate.exchange(
                "/api/bookings/offenderNo/{offenderNo}/activities/{activityId}/attendance",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {},
                "A1234AB", -11);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

//    @Test
//    public void testUpdateAttendance_WithTheValidRole() {
//        final var headers = new HttpHeaders();
//
//        final var token = jwtAuthenticationHelper.createJwt("staff1", Duration.ofDays(1), List.of("read", "write"), List.of("ROLE_PAY"));
//        final var body = Map.of("eventOutcome", "ATT", "performance", "STANDARD");
//
//        headers.add("Authorization", "Bearer " + token);
//
//        final var httpEntity = new HttpEntity(body, headers);
//
//        final var response = testRestTemplate.exchange(
//                "/api/bookings/offenderNo/{offenderNo}/activities/{activityId}/attendance",
//                HttpMethod.PUT,
//                httpEntity,
//                new ParameterizedTypeReference<String>() {},
//                "A1234AB", -11);
//
//        assertThat(response.getStatusCodeValue()).isEqualTo(203);
//    }
}
