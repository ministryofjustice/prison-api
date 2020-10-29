package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
}
