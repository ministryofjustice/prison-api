package net.syscon.elite.api.resource.impl;

import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderAssessmentResourceTest extends ResourceTest {

    @Autowired
    private AuthTokenHelper authTokenHelper;

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
}
