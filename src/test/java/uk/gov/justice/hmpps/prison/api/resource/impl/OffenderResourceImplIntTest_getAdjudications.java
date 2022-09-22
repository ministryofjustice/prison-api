package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class OffenderResourceImplIntTest_getAdjudications extends ResourceTest {

    @Test
    public void shouldReturnListOfAdjudications() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AA/adjudications",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<String>() {
            });

        final var json = getBodyAsJsonContent(response);

        Assertions.assertThat(json).extractingJsonPathArrayValue("results").hasSizeGreaterThan(0);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturn404WhenNoPrivileges() {
        // run with user that doesn't have access to the caseload
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", List.of(), Map.of());

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AA/adjudications", HttpMethod.GET, requestEntity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
