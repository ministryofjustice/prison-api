package uk.gov.justice.hmpps.prison.api.resource.impl;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

public class EducationResourceTest extends ResourceTest {
    private final String OFFENDER_NUMBER = "G8346GA";

    @Test
    public void testShouldNotBeAbleToAccessInformation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/education/prisoner/{offenderNo}",
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            OFFENDER_NUMBER
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testShouldBeAbleToAccessInformationAsASystemUser() {

        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/education/prisoner/{offenderNo}",
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            OFFENDER_NUMBER
        );

        assertThatJsonFileAndStatus(response, 200, "paged_offender_educations.json");
    }

    @Test
    public void testShouldBeAbleToAccessInformationAsAGlobalSearchUser() {

        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/education/prisoner/{offenderNo}",
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            OFFENDER_NUMBER
        );

        assertThatJsonFileAndStatus(response, 200, "paged_offender_educations.json");
    }

    @Test
    public void testShouldBeAbleToAccessInformationAsAViewPrisonerDataUser() {

        final var token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/education/prisoner/{offenderNo}",
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            OFFENDER_NUMBER
        );

        assertThatJsonFileAndStatus(response, 200, "paged_offender_educations.json");
    }

    @Test
    public void testShouldReturn404ForANonExistentOffender() {

        final var token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/education/prisoner/{offenderNo}",
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            "non_existent_nomisid"
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void testShouldNotBeAbleToAccessInformation_POST() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, Set.of(OFFENDER_NUMBER));

        final var response = testRestTemplate.postForEntity("/api/education/prisoners", httpEntity, String.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testShouldBeAbleToAccessInformationAsASystemUser_POST() {

        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);
        final var httpEntity = createHttpEntity(token, Set.of(OFFENDER_NUMBER));

        final var response = testRestTemplate.postForEntity("/api/education/prisoners", httpEntity, String.class);

        assertThatJsonFileAndStatus(response, 200, "offender_educations.json");
    }

    @Test
    public void testShouldBeAbleToAccessInformationAsAGlobalSearchUser_POST() {

        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);
        final var httpEntity = createHttpEntity(token, Set.of(OFFENDER_NUMBER));

        final var response = testRestTemplate.postForEntity("/api/education/prisoners", httpEntity, String.class);

        assertThatJsonFileAndStatus(response, 200, "offender_educations.json");
    }

    @Test
    public void testShouldBeAbleToAccessInformationAsAViewPrisonerDataUser_POST() {

        final var token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA);
        final var httpEntity = createHttpEntity(token, Set.of(OFFENDER_NUMBER));

        final var response = testRestTemplate.postForEntity("/api/education/prisoners", httpEntity, String.class);

        assertThatJsonFileAndStatus(response, 200, "offender_educations.json");
    }
}
