package uk.gov.justice.hmpps.prison.api.resource;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class InfoIntTest extends ResourceTest {
    @Test
    public void testInfoPageContainsGitInformation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var response = testRestTemplate.exchange(
            "/info",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("git.commit.id").isNotBlank();
    }

    @Test
    public void testInfoPageReportsVersion() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var response = testRestTemplate.exchange(
            "/info",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("build.version")
            .startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
    }

    @Test
    public void testReadInfoWithCachePopulated() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/reference-domains/domains/{domain}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            "ADDRESS_TYPE");

        assertThatStatus(response, 200);

        final var infoResponse = testRestTemplate.exchange(
            "/info",
            HttpMethod.GET,
            createHttpEntity(token, null), (Class<Object>) null);

        assertThatStatus(infoResponse, 200);
    }
}
