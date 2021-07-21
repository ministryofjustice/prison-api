package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.http.HttpMethod.POST;

public class OffenderMovementsResourceIntTest_createExternalMovement extends ResourceTest {

    @Test
    public void createExternalMovement() {
        final var now = LocalDateTime.now();
        final var bookingId = -1L;

        final var body = Map.of(
            "bookingId", bookingId,
            "fromAgencyId", "HAZLWD",
            "toAgencyId", "OUT",
            "movementTime", now,
            "movementType", "REL",
            "movementReason", "CR",
            "directionCode", "OUT"
        );

        final var entity = createHttpEntity(authTokenHelper.getToken(AuthToken.NORMAL_USER), body);

        final var response = testRestTemplate.exchange(
            "/api/movements",
            POST,
            entity,
            new ParameterizedTypeReference<String>() {
            });


        assertThatJsonFileAndStatus(response, 201, "create_external_movement.json");
    }
}

