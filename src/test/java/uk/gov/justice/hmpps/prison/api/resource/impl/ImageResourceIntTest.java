package uk.gov.justice.hmpps.prison.api.resource.impl;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageResourceIntTest extends ResourceTest {
    private final String imageData = "R0lGODlhAQABAIAAAAAAAAAAACH5BAAAAAAALAAAAAABAAEAAAICTAEAOw==";

    @Test
    public void getImagesByOffender() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/images/offenders/A1234AA", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "getimagesbyoffender.json");
    }

    @Test
    public void getImagesByOffenderReturnsNotFound() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/images/offenders/UNKNOWN", HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 404);
    }

    @Test
    public void getImage() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/images/-1", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "getimage.json");
    }

    @Test
    public void getImageReturnsNotFound() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of());
        final var responseEntity = testRestTemplate.exchange("/api/images/9999", HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 404);
    }

    @Test
    public void getImageData() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of("Accept", "image/jpeg"));
        final var responseEntity = testRestTemplate.exchange("/api/images/-1/data", HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 200);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_JPEG);
    }

    @Test
    public void getImageDataReturnsNotFound() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_CASE_NOTE_EVENTS"), Map.of("Accept", "image/jpeg"));
        final var responseEntity = testRestTemplate.exchange("/api/images/9999/data", HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 404);
    }

    @Test
    public void putImageReturnsForbiddenForIncorrectRoles() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("WRONG_ROLE"), imageData);
        final var responseEntity = testRestTemplate.exchange(
            "/api/images/offender/A1234AA",
            HttpMethod.PUT,
            requestEntity,
            ImageDetail.class
        );

        assertThatStatus(responseEntity, 403);
    }

    @Test
    public void putImageReturnsNotFoundForInvalidOffender() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_SYSTEM_USER"), imageData);
        final var responseEntity = testRestTemplate.exchange(
            "/api/images/offender/A9999XX",
            HttpMethod.PUT,
            requestEntity,
            ImageDetail.class);

        assertThatStatus(responseEntity, 404);
    }

    @Test
    public void putImageUploadsThumbnailByDefault() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_SYSTEM_USER"), imageData);
        final var responseEntity = testRestTemplate.exchange(
            "/api/images/offender/A1234AI",
            HttpMethod.PUT,
            requestEntity,
            ImageDetail.class);

        assertThatStatus(responseEntity, 200);
        assertThat(responseEntity.getBody()).isInstanceOf(ImageDetail.class);
        final var imageDetail = responseEntity.getBody();
        assertThat(imageDetail).isNotNull();
        assertThat(imageDetail.getImageId()).isGreaterThan(0);
        assertThat(imageDetail.getCaptureDate()).isAfter(LocalDate.now().minusDays(1));
        assertThat(imageDetail.getImageType()).isEqualTo("OFF_BKG");
    }

    @Test
    public void putImageUploadsFullSizeImage() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_SYSTEM_USER"), imageData);
        final var responseEntity = testRestTemplate.exchange(
            "/api/images/offender/A1234AI?fullSizeImage=true",
            HttpMethod.PUT,
            requestEntity,
            ImageDetail.class);

        assertThatStatus(responseEntity, 200);
        assertThat(responseEntity.getBody()).isInstanceOf(ImageDetail.class);
        final var imageDetail = responseEntity.getBody();
        assertThat(imageDetail).isNotNull();
        assertThat(imageDetail.getImageId()).isGreaterThan(0);
        assertThat(imageDetail.getCaptureDate()).isAfter(LocalDate.now().minusDays(1));
        assertThat(imageDetail.getImageType()).isEqualTo("OFF_BKG");
    }
}
