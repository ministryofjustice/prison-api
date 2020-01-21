package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import net.syscon.elite.repository.OffenderDeletionRepository;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static net.syscon.elite.executablespecification.steps.AuthTokenHelper.AuthToken.ELITE2_API_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OffendersResourceTest extends ResourceTest {

    private final String OFFENDER_NUMBER = "A1234AB";

    @MockBean
    private OffenderDeletionRepository offenderDeletionRepository;

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
                OFFENDER_NUMBER);

        assertThatJsonFileAndStatus(response, 200, "sentence.json");
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
                OFFENDER_NUMBER);

        assertThatJsonFileAndStatus(response, 200, "sentence.json");
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
                OFFENDER_NUMBER);

        assertThatJsonFileAndStatus(response, 200, "alerts.json");
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
                OFFENDER_NUMBER);

        assertThatJsonFileAndStatus(response, 200, "casenotes.json");
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
                OFFENDER_NUMBER);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void testGetIncidents() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_READ_ONLY);

        final var response = testRestTemplate.exchange(
                "/api/incidents/-1",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<IncidentCase>() {
                });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final var result = response.getBody();
        assertThat(result).extracting("incidentCaseId", "incidentTitle", "incidentType")
                .containsExactlyInAnyOrder(-1L, "Big Fight", "ASSAULT");
        assertThat(result.getResponses()).hasSize(19);
        assertThat(result.getParties()).hasSize(6);
    }

    @Test
    public void testGetIncidentsNoParties() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_READ_ONLY);

        final var response = testRestTemplate.exchange(
                "/api/incidents/-4",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<IncidentCase>() {
                });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting("incidentCaseId", "incidentTitle")
                .containsExactlyInAnyOrder(-4L, "Medium sized fight");
    }

    @Test
    public void testCanRetrieveIncidentCandidatesWithSystemUser() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_READ_ONLY);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/incidents/candidates?fromDateTime=2016-02-02T14:00:00",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "incidents_candidates.json");
    }

    @Test
    public void testCanRetrieveIncidentCandidatesPage() {
        final var paging = new HashMap<String, String>();
        paging.put("Page-Offset", "1");
        paging.put("Page-Limit", "2");
        final var httpEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_SYSTEM_READ_ONLY"), paging);

        final var response = testRestTemplate.exchange(
                "/api/offenders/incidents/candidates?fromDateTime=2016-02-02T14:00:00",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getHeaders().get("Page-Offset")).containsExactly("1");
        assertThat(response.getHeaders().get("Page-Limit")).containsExactly("2");
        assertThat(response.getHeaders().get("Total-Records")).containsExactly("4");
        assertThatJsonFileAndStatus(response, 200, "incidents_candidates_page.json");
    }

    @Test
    public void testCannotRetrieveIncidentCandidatesWithGlobalSearch() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/incidents/candidates?fromDateTime=2016-02-02T14:00:00",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testCanRetrieveAlertCandidatesWithSystemUser() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/alerts/candidates?fromDateTime=2016-02-02T14:00:00",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "alerts_candidates.json");
    }

    @Test
    public void testCanRetrieveAlertCandidatesPage() {
        final var paging = new HashMap<String, String>();
        paging.put("Page-Offset", "1");
        paging.put("Page-Limit", "2");
        final var httpEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_SYSTEM_READ_ONLY"), paging);

        final var response = testRestTemplate.exchange(
                "/api/offenders/alerts/candidates?fromDateTime=2016-02-02T14:00:00",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getHeaders().get("Page-Offset")).containsExactly("1");
        assertThat(response.getHeaders().get("Page-Limit")).containsExactly("2");
        assertThat(response.getHeaders().get("Total-Records")).containsExactly("2");
        assertThatJsonFileAndStatus(response, 200, "alerts_candidates_page.json");
    }

    @Test
    public void testCannotRetrieveAlertCandidatesWithGlobalSearch() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/alerts/candidates?fromDateTime=2016-02-02T14:00:00",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void deleteOffender() {

        assertThatStatus(
                deleteOffenderWithRoles("ROLE_DELETE_OFFENDER"),
                204);

        verify(offenderDeletionRepository).deleteOffender(OFFENDER_NUMBER);
    }

    @Test
    public void deleteOffenderForbiddenWithoutCorrectRole() {

        assertThatStatus(
                deleteOffenderWithRoles("ROLE_IS_INCORRECT"),
                403);

        verify(offenderDeletionRepository, never()).deleteOffender(any());
    }

    private ResponseEntity<Void> deleteOffenderWithRoles(final String... roles) {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("SOME_USER", asList(roles), Map.of());
        return testRestTemplate.exchange("/api/offenders/" + OFFENDER_NUMBER, HttpMethod.DELETE, requestEntity, Void.class);
    }

    @Test
    public void listAllOffenders() {

        ResponseEntity<String> response = listAllOffendersUsingHeaders(
                Map.of("Page-Offset", "0", "Page-Limit", "100"));

        assertThatJsonFileAndStatus(response, 200, "list_all_offenders.json");

        assertThat(response.getHeaders().get("Page-Offset")).containsExactly("0");
        assertThat(response.getHeaders().get("Page-Limit")).containsExactly("100");
        assertThat(response.getHeaders().get("Total-Records")).containsExactly("51");
    }

    @Test
    public void listAllOffendersUsesDefaultPaginationParams() {

        ResponseEntity<String> response = listAllOffendersUsingHeaders(Map.of());

        assertThatJsonFileAndStatus(response, 200, "list_all_offenders.json");

        assertThat(response.getHeaders().get("Page-Offset")).containsExactly("0");
        assertThat(response.getHeaders().get("Page-Limit")).containsExactly("100");
        assertThat(response.getHeaders().get("Total-Records")).containsExactly("51");
    }

    private ResponseEntity<String> listAllOffendersUsingHeaders(final Map<String, String> headers) {
        final var requestEntity = createHttpEntity(authTokenHelper.getToken(ELITE2_API_USER), null, headers);
        return testRestTemplate.exchange("/api/offenders/ids", HttpMethod.GET, requestEntity, String.class);
    }
}
