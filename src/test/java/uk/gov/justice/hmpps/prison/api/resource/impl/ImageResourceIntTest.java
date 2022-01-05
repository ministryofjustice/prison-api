package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.PRISON_API_USER;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE;

@TestMethodOrder(OrderAnnotation.class)
public class ImageResourceIntTest extends ResourceTest {

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
    @Order(1)
    public void getOffendersWithImages() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE), null);

        final var responseEntity = testRestTemplate.exchange(
            "/api/images/offenders?fromDateTime={fromDateTime}",
            HttpMethod.GET, requestEntity, String.class, LocalDateTime.of(2020, 1, 1, 0, 0));

        assertThatStatus(responseEntity, 200);
        assertThat(getBodyAsJsonContent(responseEntity)).isStrictlyEqualToJson("offenders_with_images.json");
    }

    @Test
    @Order(2)
    public void getOffendersWithImagesPaged() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE), null);

        final var responseEntity = testRestTemplate.exchange(
            "/api/images/offenders?fromDateTime={fromDateTime}&paged=true&size=3&page=1",
            HttpMethod.GET, requestEntity, String.class, LocalDateTime.of(2020, 1, 1, 0, 0));

        assertThatStatus(responseEntity, 200);
        assertThat(getBodyAsJsonContent(responseEntity)).isStrictlyEqualToJson("offenders_with_images_paged.json");
    }

    @Test
    @Order(3)
    public void getOffendersWithImagesIsBadRequestWhenDateMissing() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE), null);

        final var responseEntity = testRestTemplate.exchange(
            "/api/images/offenders",
            HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 400);
    }

    @Test
    @Order(4)
    public void getOffendersWithImagesIsForbiddenWhenNonSystemUser() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(PRISON_API_USER), null);

        final var responseEntity = testRestTemplate.exchange(
            "/api/images/offenders?fromDateTime={fromDateTime}",
            HttpMethod.GET, requestEntity, String.class, LocalDateTime.of(2020, 1, 1, 0, 0));

        assertThatStatus(responseEntity, 403);
    }

    @Test
    public void putImageReturnsForbiddenForIncorrectRoles() {
        final var requestEntity = generateMultiPartFormRequest(List.of("WRONG_ROLE"));
        final var responseEntity = testRestTemplate.exchange(
            "/api/images/offenders/A1234AA",
            HttpMethod.POST,
            requestEntity,
            ImageDetail.class
        );

        assertThatStatus(responseEntity, 403);
    }

    @Test
    public void putImageReturnsNotFoundForInvalidOffender() {
        final var requestEntity = generateMultiPartFormRequest(List.of("ROLE_IMAGE_UPLOAD"));
        final var responseEntity = testRestTemplate.exchange(
            "/api/images/offenders/A9999XX",
            HttpMethod.POST,
            requestEntity,
            ImageDetail.class);

        assertThatStatus(responseEntity, 404);
    }

    @Test
    public void putImageUploadsAndStoresScaledImages() {
        final var requestEntity = generateMultiPartFormRequest(List.of("ROLE_IMAGE_UPLOAD"));
        final var responseEntity = testRestTemplate.postForEntity(
            "/api/images/offenders/A1234AI",
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

    @NotNull
    private HttpEntity<LinkedMultiValueMap<String, Object>> generateMultiPartFormRequest(final List<String> roles)  {

        File file = new File(Objects.requireNonNull(getClass().getResource("/images/image.jpg")).getFile());
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new FileSystemResource(file));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        final var jwt = createJwt("ITAG_USER", roles);
        httpHeaders.add("Authorization", "Bearer " + jwt);

        return new HttpEntity<LinkedMultiValueMap<String, Object>>(parts, httpHeaders);

    }
}
