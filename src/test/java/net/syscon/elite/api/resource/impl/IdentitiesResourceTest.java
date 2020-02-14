package net.syscon.elite.api.resource.impl;

import net.syscon.elite.executablespecification.steps.AuthTokenHelper.AuthToken;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

public class IdentitiesResourceTest extends ResourceTest {

    @Test
    public void testCanFindIdentitiesByType() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/identifiers?type={type}",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                }, "-4", "PNC");

        assertThatJsonFileAndStatus(response, 200, "identities_by_type.json");
    }

    @Test
    public void testCanFindIdentitiesNoType() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/identifiers",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                }, "-4");

        assertThatJsonFileAndStatus(response, 200, "all_identities_by_type.json");
    }


}
