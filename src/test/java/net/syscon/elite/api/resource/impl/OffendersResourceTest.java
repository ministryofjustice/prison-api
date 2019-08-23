package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.OffenderSentenceDetail;
import net.syscon.elite.api.model.v1.Events;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.ResolvableType.forType;

public class OffendersResourceTest extends ResourceTest {

    @Autowired
    private AuthTokenHelper authTokenHelper;

    @Test
    public void testCanRetrieveSentenceDetailsForOffender() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/{nomsId}/sentences",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "A1234AB");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        assertThat(new JsonContent<Events>(getClass(), forType(OffenderSentenceDetail.class), response.getBody())).isEqualToJson("sentence.json");
    }

    @Test
    public void testCanRetrieveSentenceDetailsForOffenderWithSystemUser() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/{nomsId}/sentences",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "A1234AB");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        assertThat(new JsonContent<Events>(getClass(), forType(OffenderSentenceDetail.class), response.getBody())).isEqualToJson("sentence.json");
    }

    @Test
    public void testCanRetrieveAlertsForOffenderWithGlobalSearch() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/{nomsId}/alerts",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "A1234AB");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        assertThat(new JsonContent<Events>(getClass(), forType(Alert.class), response.getBody())).isEqualToJson("alerts.json");
    }

    @Test
    public void testCanRetrieveCaseNotesForOffender() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/{nomsId}/case-notes",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "A1234AB");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        assertThat(new JsonContent<Events>(getClass(), forType(CaseNote.class), response.getBody())).isEqualToJson("casenotes.json");
    }

    @Test
    public void testCannotRetrieveCaseNotesForOffenderWithGlobalSearch() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/{nomsId}/case-notes",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "A1234AB");

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }
}
