package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderResourceImplIntTest_getOffenderRestrictions extends ResourceTest {

    @Test
    public void shouldReturnListOfActiveOffenderRestrictions() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1234AH/offender-restrictions",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        final var json = getBodyAsJsonContent(response);
        assertThat(json).extractingJsonPathNumberValue("bookingId").isEqualTo(-8);
        assertThat(json).extractingJsonPathArrayValue("offenderRestrictions").hasSize(2);
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[0].restrictionTypeDescription").isEqualTo("Restricted");
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[0].restrictionType").isEqualTo("RESTRICTED");
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[0].startDate").isEqualTo("2001-01-01");
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[0].expiryDate").isNull();
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[0].comment").isEqualTo("Some Comment Text");
        assertThat(json).extractingJsonPathBooleanValue("offenderRestrictions[0].active").isTrue();
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[1].restrictionTypeDescription").isEqualTo("Child Visitors to be Vetted");
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[1].restrictionType").isEqualTo("CHILD");
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[1].startDate").isEqualTo("2001-01-01");
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[1].expiryDate").isNull();
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[1].comment").isEqualTo("More Comment Text");
        assertThat(json).extractingJsonPathBooleanValue("offenderRestrictions[1].active").isTrue();
    }

    @Test
    public void shouldReturnListOfAllOffenderRestrictions() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1234AH/offender-restrictions?activeRestrictionsOnly=false",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        final var json = getBodyAsJsonContent(response);
        assertThat(json).extractingJsonPathNumberValue("bookingId").isEqualTo(-8);
        assertThat(json).extractingJsonPathArrayValue("offenderRestrictions").hasSize(3);
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[0].restrictionType").isEqualTo("RESTRICTED");
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[1].restrictionType").isEqualTo("CHILD");
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[2].restrictionType").isEqualTo("BAN");
        assertThat(json).extractingJsonPathStringValue("offenderRestrictions[2].expiryDate").isEqualTo("2002-01-01");
    }


    @Test
    public void shouldReturn404WhenOffenderNotFound() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/AAA444/offender-restrictions",
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
                "/api/offenders/A1234AH/offender-restrictions",
                HttpMethod.GET,
                request,
                ErrorResponse.class);

        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }
}
