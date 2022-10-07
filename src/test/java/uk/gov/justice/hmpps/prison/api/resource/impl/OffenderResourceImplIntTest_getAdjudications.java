package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class OffenderResourceImplIntTest_getAdjudications extends ResourceTest {

    @Test
    public void shouldReturnListOfAdjudicationsForUserWithCaseload() {

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AA/adjudications",
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of()),
            new ParameterizedTypeReference<String>() {
            });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final var json = getBodyAsJsonContent(response);
        assertThat(json).extractingJsonPathArrayValue("results").isNotEmpty();
        assertThat(json).extractingJsonPathArrayValue("offences").isNotEmpty();
        assertThat(json).extractingJsonPathArrayValue("agencies").isNotEmpty();
    }

    @Test
    public void shouldReturn404WhenNoPrivileges() {
        // run with user that doesn't have access to the caseload

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AA/adjudications",
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", List.of(), Map.of()), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnListOfAdjudicationsForViewAdjudicationsRole() {

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AA/adjudications",
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", List.of("ROLE_VIEW_ADJUDICATIONS"), Map.of()),
            new ParameterizedTypeReference<String>() {
            }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final var json = getBodyAsJsonContent(response);
        Assertions.assertThat(json).extractingJsonPathArrayValue("results").isNotEmpty();
    }
}
