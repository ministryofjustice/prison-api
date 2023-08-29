package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderResourceImplIntTest_getNonAssociationDetails extends ResourceTest {

    @Test
    public void shouldReturnListOfNonAssociations() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1234AA/non-association-details",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        final var json = getBodyAsJsonContent(response);
        assertThat(json).extractingJsonPathStringValue("offenderNo").isEqualTo("A1234AA");
        assertThat(json).extractingJsonPathStringValue("firstName").isEqualTo("Arthur");
        assertThat(json).extractingJsonPathStringValue("lastName").isEqualTo("Anderson");
        assertThat(json).extractingJsonPathStringValue("agencyId").isEqualTo("LEI");
        assertThat(json).extractingJsonPathArrayValue("nonAssociations").hasSize(2);
        assertThat(json).extractingJsonPathStringValue("nonAssociations[0].reasonCode").isEqualTo("VIC");
        assertThat(json).extractingJsonPathStringValue("nonAssociations[0].offenderNonAssociation.offenderNo").isEqualTo("A1179MT");
        assertThat(json).extractingJsonPathStringValue("nonAssociations[0].offenderNonAssociation.firstName").isEqualTo("Marcus");
        assertThat(json).extractingJsonPathStringValue("nonAssociations[0].offenderNonAssociation.agencyId").isEqualTo("MDI");

        assertThat(json).extractingJsonPathStringValue("nonAssociations[1].offenderNonAssociation.offenderNo").isEqualTo("A1234AC");
        assertThat(json).extractingJsonPathStringValue("nonAssociations[1].offenderNonAssociation.firstName").isEqualTo("Norman");
        assertThat(json).extractingJsonPathStringValue("nonAssociations[1].offenderNonAssociation.agencyId").isEqualTo("LEI");
    }

    @Test
    public void shouldReturnListOfNonAssociationsCurrentPrisonOnly() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1179MT/non-association-details?currentPrisonOnly=true",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<String>() {
            });

        final var json = getBodyAsJsonContent(response);
        assertThat(json).extractingJsonPathStringValue("offenderNo").isEqualTo("A1179MT");
        assertThat(json).extractingJsonPathStringValue("firstName").isEqualTo("Marcus");
        assertThat(json).extractingJsonPathStringValue("lastName").isEqualTo("Trescothick");
        assertThat(json).extractingJsonPathStringValue("agencyId").isEqualTo("MDI");
        assertThat(json).extractingJsonPathArrayValue("nonAssociations").hasSize(0);
    }

    @Test
    public void shouldReturnListOfNonAssociationsActiveOnlyReverse() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AA/non-association-details?currentPrisonOnly=false&excludeInactive=true",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<String>() {
            });

        final var json = getBodyAsJsonContent(response);
        assertThat(json).extractingJsonPathStringValue("offenderNo").isEqualTo("A1234AA");
        assertThat(json).extractingJsonPathStringValue("firstName").isEqualTo("Arthur");
        assertThat(json).extractingJsonPathStringValue("lastName").isEqualTo("Anderson");
        assertThat(json).extractingJsonPathStringValue("agencyId").isEqualTo("LEI");
        assertThat(json).extractingJsonPathArrayValue("nonAssociations").hasSize(1);
        assertThat(json).extractingJsonPathStringValue("nonAssociations[0].reasonCode").isEqualTo("VIC");
        assertThat(json).extractingJsonPathStringValue("nonAssociations[0].offenderNonAssociation.offenderNo").isEqualTo("A1179MT");
        assertThat(json).extractingJsonPathStringValue("nonAssociations[0].offenderNonAssociation.firstName").isEqualTo("Marcus");
        assertThat(json).extractingJsonPathStringValue("nonAssociations[0].offenderNonAssociation.agencyId").isEqualTo("MDI");
    }

    @Test
    public void shouldReturnListOfNonAssociationsActiveOnly() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/A1234AC/non-association-details?excludeInactive=true",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<String>() {
            });

        final var json = getBodyAsJsonContent(response);
        assertThat(json).extractingJsonPathStringValue("offenderNo").isEqualTo("A1234AC");
        assertThat(json).extractingJsonPathStringValue("firstName").isEqualTo("Norman");
        assertThat(json).extractingJsonPathStringValue("lastName").isEqualTo("Bates");
        assertThat(json).extractingJsonPathArrayValue("nonAssociations").hasSize(0);
    }

    @Test
    public void shouldReturn404WhenOffenderNotFound() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/AAA444/non-association-details",
                HttpMethod.GET,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Resource with id [AAA444] not found.")
                        .developerMessage("Resource with id [AAA444] not found.")
                        .build());
    }

    @Test
    public void shouldReturn404IfNotAuthorised() {

        final var token = authTokenHelper.getToken(AuthToken.UNAUTHORISED_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1234AA/non-association-details",
                HttpMethod.GET,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Resource with id [A1234AA] not found.")
                        .developerMessage("Resource with id [A1234AA] not found.")
                        .build());
    }
}
