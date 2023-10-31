package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderResourceImplIntTest_getOffenderContacts extends ResourceTest {

    @Test
    public void shouldReturnListOfContacts() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1234AH/contacts",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        final var json = getBodyAsJsonContent(response);
        assertThat(json).extractingJsonPathArrayValue("offenderContacts").hasSize(3);
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].lastName").isEqualTo("Johnson");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].firstName").isEqualTo("John");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].middleName").isEqualTo("Justice");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].contactType").isEqualTo("S");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].contactTypeDescription").isEqualTo("Social/Family");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].relationshipCode").isEqualTo("FRI");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].emails[0].email").isEqualTo("visitor@other.com");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].relationshipDescription").isEqualTo("Friend");
        assertThat(json).extractingJsonPathNumberValue("offenderContacts[0].bookingId").isEqualTo(-8);
        assertThat(json).extractingJsonPathNumberValue("offenderContacts[0].personId").isEqualTo(-3);
        assertThat(json).extractingJsonPathBooleanValue("offenderContacts[0].nextOfKin").isTrue();
        assertThat(json).extractingJsonPathBooleanValue("offenderContacts[0].emergencyContact").isTrue();
        assertThat(json).extractingJsonPathBooleanValue("offenderContacts[0].approvedVisitor").isTrue();
        assertThat(json).extractingJsonPathNumberValue("offenderContacts[0].restrictions[0].restrictionId").isEqualTo(13520);
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].restrictions[0].comment").isEqualTo("a comment");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].restrictions[0].restrictionType").isEqualTo("CLOSED");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].restrictions[0].restrictionTypeDescription").isEqualTo("Closed");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].restrictions[0].startDate").isEqualTo("2021-10-15");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].restrictions[0].expiryDate").isEqualTo("2026-10-13");
        assertThat(json).extractingJsonPathBooleanValue("offenderContacts[0].restrictions[0].globalRestriction").isTrue();
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].restrictions[1].comment").isEqualTo("Some Comment Text");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].restrictions[1].restrictionType").isEqualTo("BAN");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].restrictions[1].restrictionTypeDescription").isEqualTo("Banned");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].restrictions[1].startDate").isEqualTo("2001-01-01");
        assertThat(json).extractingJsonPathStringValue("offenderContacts[0].restrictions[1].expiryDate").isNull();
        assertThat(json).extractingJsonPathNumberValue("offenderContacts[0].restrictions[1].restrictionId").isEqualTo(-2);
        assertThat(json).extractingJsonPathBooleanValue("offenderContacts[0].restrictions[1].globalRestriction").isFalse();
        assertThat(json).extractingJsonPathBooleanValue("offenderContacts[0].active").isTrue();
        assertThat(json).extractingJsonPathBooleanValue("offenderContacts[1].approvedVisitor").isFalse();
        assertThat(json).extractingJsonPathBooleanValue("offenderContacts[1].active").isTrue();
        assertThat(json).extractingJsonPathArrayValue("offenderContacts[1].restrictions").isEmpty();
        assertThat(json).extractingJsonPathBooleanValue("offenderContacts[2].active").isFalse();
    }

    @Test
    public void shouldReturnVisitorApprovedListOfContacts() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1234AH/contacts?approvedVisitorsOnly=true",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(getBodyAsJsonContent(response)).extractingJsonPathArrayValue("offenderContacts").hasSize(1);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("offenderContacts[0].personId").isEqualTo(-3);
    }

    @Test
    public void shouldReturnListOfActiveContacts() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1234AH/contacts?activeOnly=true",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(getBodyAsJsonContent(response)).extractingJsonPathArrayValue("offenderContacts")
                .extracting("active").containsExactly(true, true);
    }


    @Test
    public void shouldReturn404WhenOffenderNotFound() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1774AA/contacts",
                HttpMethod.GET,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Resource with id [A1774AA] not found.")
                        .developerMessage("Resource with id [A1774AA] not found.")
                        .build());
    }

    @Test
    public void shouldReturn403IfNotAuthorised() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/A1234AH/contacts",
                HttpMethod.GET,
                request,
                ErrorResponse.class);

        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }
}
