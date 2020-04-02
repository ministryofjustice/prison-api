package net.syscon.elite.api.resource.impl;

import net.syscon.elite.util.JwtParameters;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;

// All data required for these tests can be found in R__8_7_MOVE_TO_CELL.sql
public class OffenderMovementsResourceImplIntTest_moveToCell extends ResourceTest {

    @Test
    public void moveToCell_validRequest_returnsOkAndExpcetedData() {
        final var token = mtcUser();

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                format("/api/bookings/-56/living-unit/-301?reasonCode=ADM&dateTime=%s", LocalDateTime.now().minusHours(1).format(ISO_LOCAL_DATE_TIME)),
                PUT,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.bookingId").isEqualTo(-56);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.internalLocationId").isEqualTo("-301");
    }


    private String mtcUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("MTC_USER")
                        .scope(List.of("read", "write"))
                        .roles(emptyList())
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

}
