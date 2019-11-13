package net.syscon.elite.api.resource.impl;

import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.value;
import static org.assertj.core.api.Assertions.assertThat;

public class OffenderAssessmentResourceTest extends ResourceTest {
    @Test
    public void testSystemUserCanUpdateCategoryNextReviewDate() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/nextReviewDate/{nextReviewDate}",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-1", "2018-06-05");

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void testNormalUserCannotUpdateCategoryNextReviewDate() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/nextReviewDate/{nextReviewDate}",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-1", "2018-06-05");

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void testUpdateCategoryNextReviewDateActiveCategorisationDoesNotExist() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/nextReviewDate/{nextReviewDate}",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-56", "2018-06-05");

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testSystemUserCanUpdateCategorySetActiveInactive() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        // choose a booking that doesnt actually have any active
        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/inactive",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-34");

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void testSystemUserCanUpdateCategorySetPendingInactive() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        // choose a booking that doesnt actually have any active
        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/inactive?status=PENDING",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-31");

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void testSetPendingInactiveValidationError() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/inactive?status=OTHER",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-34");

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody()).contains("Assessment status type is invalid: OTHER");
    }

    @Test
    public void testNormalUserCannotUpdateCategorySetInactive() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/inactive",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                }, "-1");

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void testGetOffenderCategorisationsPost() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, List.of("-1", "-2", "-3", "-38", "-39", "-40", "-41"));

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/LEI?latest=false",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThatJson(response.getBody()).isArray().hasSize(1);
        assertThatJson(response.getBody()).node("[0].bookingId").isEqualTo(value(-1));
    }

    @Test
    public void testGetOffenderCategorisationsSystem() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_READ_ONLY);

        final var httpEntity = createHttpEntity(token, List.of("-1", "-2", "-3", "-38", "-39", "-40", "-41"));

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category?latest=false",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThatJson(response.getBody()).isArray().hasSize(6);
        assertThatJson(response.getBody()).node("[0].bookingId").isEqualTo(value(-1));
        assertThatJson(response.getBody()).node("[1].bookingId").isEqualTo(value(-3));
        assertThatJson(response.getBody()).node("[2].bookingId").isEqualTo(value(-38));
        assertThatJson(response.getBody()).node("[3].bookingId").isEqualTo(value(-39));
        assertThatJson(response.getBody()).node("[4].bookingId").isEqualTo(value(-40));
        assertThatJson(response.getBody()).node("[5].bookingId").isEqualTo(value(-41));
    }
}
