package uk.gov.justice.hmpps.nomis.datacompliance.controller;

import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.FORBIDDEN_403;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.springframework.http.HttpMethod.GET;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.PRISON_API_USER;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE;

public class DataComplianceControllerTest extends ResourceTest {

    @Test
    public void requestOffendersWithImages() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE), null);

        final var response = testRestTemplate.exchange(
                "/api/data-compliance/offenders-with-images?fromDateTime={fromDateTime}",
                GET, requestEntity, String.class, LocalDateTime.of(2020, 1, 1, 0, 0));

        assertThat(response.getStatusCodeValue()).isEqualTo(OK_200);
        assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("offenders_with_images.json");
    }

    @Test
    public void requestOffendersWithImagesPaged() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE), null);

        final var response = testRestTemplate.exchange(
                "/api/data-compliance/offenders-with-images?fromDateTime={fromDateTime}&paged=true&size=3&page=1",
                GET, requestEntity, String.class, LocalDateTime.of(2020, 1, 1, 0, 0));

        assertThat(response.getStatusCodeValue()).isEqualTo(OK_200);
        assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("offenders_with_images_paged.json");
    }

    @Test
    public void requestOffendersWithImagesIsBadRequestWhenDateMissing() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE), null);

        final var response = testRestTemplate.exchange(
                "/api/data-compliance/offenders-with-images",
                GET, requestEntity, String.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(BAD_REQUEST_400);
    }

    @Test
    public void requestOffendersWithImagesIsForbiddenWhenNonSystemUser() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(PRISON_API_USER), null);

        final var response = testRestTemplate.exchange(
                "/api/data-compliance/offenders-with-images?fromDateTime={fromDateTime}",
                GET, requestEntity, String.class, LocalDateTime.of(2020, 1, 1, 0, 0));

        assertThat(response.getStatusCodeValue()).isEqualTo(FORBIDDEN_403);
    }
}
