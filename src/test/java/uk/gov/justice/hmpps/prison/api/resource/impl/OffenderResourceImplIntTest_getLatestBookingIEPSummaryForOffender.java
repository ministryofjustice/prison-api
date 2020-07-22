package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderResourceImplIntTest_getLatestBookingIEPSummaryForOffender extends ResourceTest {

    @Test
    public void shouldReturnIEPSummaryWithDetail() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1234AA/iepSummary?withDetails=true",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "iep_summary_with_detail_A1234AA.json");
    }

    @Test
    public void shouldReturnIEPSummary() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1234AA/iepSummary",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "iep_summary_A1234AA.json");
    }

    @Test
    public void shouldReturn404WhenOffenderNotFound() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1554AN/iepSummary",
                HttpMethod.GET,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Resource with id [A1554AN] not found.")
                        .developerMessage("Resource with id [A1554AN] not found.")
                        .build());
    }
}
