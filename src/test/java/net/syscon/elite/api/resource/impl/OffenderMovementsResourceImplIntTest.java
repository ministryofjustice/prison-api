package net.syscon.elite.api.resource.impl;

import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Map;

public class OffenderMovementsResourceImplIntTest extends ResourceTest {

    @Test
    public void schedule_court_hearing() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, Map.of(
                "courtCaseId", -1,
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 201, "court_hearing.json");
    }
}
