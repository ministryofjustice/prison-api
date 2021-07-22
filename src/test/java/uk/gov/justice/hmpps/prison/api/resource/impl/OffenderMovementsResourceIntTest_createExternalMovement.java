package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpMethod.POST;

public class OffenderMovementsResourceIntTest_createExternalMovement extends ResourceTest {

    @Test
    public void createExternalMovement() {

        final var entity = createHttpEntity(authTokenHelper.getToken(AuthToken.INACTIVE_BOOKING_USER), getBody());

        final var response = testRestTemplate.exchange(
            "/api/movements",
            POST,
            entity,
            new ParameterizedTypeReference<String>() {
            });


        assertThatJsonFileAndStatus(response, 201, "create_external_movement.json");
    }

    @Test
    public void returnNotAuthorised_whenTheUserIsMissingTheCorrectRole() {
       final var entity = createHttpEntity(authTokenHelper.getToken(AuthToken.NORMAL_USER), getBody());

        final var response = testRestTemplate.exchange(
            "/api/movements",
            POST,
            entity,
            new ParameterizedTypeReference<String>() {
            });

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    private Map<String,Object> getBody() {
        return  Map.of(
            "bookingId", -21,
            "fromAgencyId", "HAZLWD",
            "toAgencyId", "OUT",
            "movementTime", LocalDateTime.now(),
            "movementType", "TRN",
            "movementReason", "SEC",
            "directionCode", "OUT"
        );
    }
}

