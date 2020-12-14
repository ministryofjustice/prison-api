package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.PRISON_API_USER;

@ContextConfiguration(classes = OffendersResourceTest.TestClock.class)
public class OffendersResourceTest extends ResourceTest {

    @TestConfiguration
    static class TestClock {
        private LocalDateTime timeIs_2020_10_01T000000 = LocalDate.parse("2020-10-01", DateTimeFormatter.ISO_DATE).atStartOfDay();

        @Bean
        public Clock clock() {
            return Clock.fixed(timeIs_2020_10_01T000000.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        }
    }

    private final String OFFENDER_NUMBER = "A1234AB";

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
    public void testCanRetrieveAlertsForOffenderWithViewDataRole() {
        final var token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA);

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
    public void testCannotRetrieveCaseNotesForOffenderWithViewPrisonerData() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

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
    public void getFullOffenderInformation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            OFFENDER_NUMBER);

        assertThatJsonFileAndStatus(response, 200, "offender_detail.json");
    }

    @Test
    public void testOffenderWithActiveRecallOffence() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            "A1234AC");

        assertThatJsonFileAndStatus(response, 200, "offender_detail_recall.json");
    }

    @Test
    public void testOffenderWithInActiveRecallOffence() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            "A1234AD");

        assertThatJsonFileAndStatus(response, 200, "offender_detail_no_recall.json");
    }

    @Test
    public void getOffenderInformationWithoutBooking() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            "A1234DD");

        assertThatJsonFileAndStatus(response, 200, "offender_detail_min.json");
    }

    @Test
    public void getOffenderNotFound() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            "B1234DD");

        assertThatStatus(response, 404);
    }


    @Test
    public void getFullOffenderInformation_WithAliases() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            "A1234AI");

        assertThatJsonFileAndStatus(response, 200, "offender_detail_aliases.json");
    }

    @Test
    public void testGetIncidents() {
        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);

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
        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);

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
        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);

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
        final var httpEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_SYSTEM_USER"), paging);

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
    public void testCannotRetrieveIncidentCandidatesWithViewPrisonerDataRole() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

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
        final var httpEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_SYSTEM_USER"), paging);

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
    public void testCannotRetrieveAlertCandidatesWithViewData() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

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
    public void listAllOffenders() {

        ResponseEntity<String> response = listAllOffendersUsingHeaders(
            Map.of("Page-Offset", "0", "Page-Limit", "100"));

        assertThatJsonFileAndStatus(response, 200, "list_all_offenders.json");

        assertThat(response.getHeaders().get("Page-Offset")).containsExactly("0");
        assertThat(response.getHeaders().get("Page-Limit")).containsExactly("100");
        assertThat(response.getHeaders().get("Total-Records")).containsExactly("52");
    }

    @Test
    public void testCannotReleasePrisonerInTheFuture() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of("movementReasonCode", "CR",
            "commentText", "released prisoner today",
            "releaseTime", LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        final var entity = createHttpEntity(token, body);

        final var prisonerNo = "A1234AA";
        final var response =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/release",
            PUT,
            entity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    //
    @Test
    public void testCannotReleasePrisonerBeforeLastMovement() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of("movementReasonCode", "CR",
            "commentText", "released prisoner today",
            "releaseTime", LocalDateTime.of(2019, 10, 17, 17, 29, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        final var entity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/release",
            PUT,
            entity,
            new ParameterizedTypeReference<String>() {
            },
            "A1234AA"
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void testCanReleaseAPrisoner() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of("movementReasonCode", "CR",
            "commentText", "released prisoner today",
            "releaseTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        final var entity = createHttpEntity(token, body);

        final var prisonerNo = "A1181MV";
        final var response =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/release",
            PUT,
            entity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        // check that prisoner is now out
        final var searchToken  = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);
        final var httpEntity = createHttpEntity(searchToken, format("{ \"offenderNos\": [ \"%s\" ] }", prisonerNo));

        final var searchResponse = testRestTemplate.exchange(
            "/api/prisoners",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(searchResponse, 200, "released_prisoner.json");
    }

    @Test
    public void testCanTransferAPrisoner() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var now = LocalDateTime.now();
        final var body = Map.of("transferReasonCode", "NOTR", "commentText", "transferred prisoner today", "toLocation", "MDI",
            "movementTime", now.minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        final var entity = createHttpEntity(token, body);

        final var prisonerNo = "A1180HI";
        final var response =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/transfer-out",
            PUT,
            entity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        // check that prisoner is now out
        final var searchToken  = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);
        final var httpEntity = createHttpEntity(searchToken, format("{ \"offenderNos\": [ \"%s\" ] }", prisonerNo));

        final var searchResponse = testRestTemplate.exchange(
            "/api/prisoners",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(searchResponse, 200, "transferred_out_prisoner.json");

        final var tranferInRequest = Map.of("receiveTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "commentText", "admitted",
            "cellLocation", "MDI-1-3-022");

        final var tranferInEntity = createHttpEntity(token, tranferInRequest);

        final var transferInResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/transfer-in",
            PUT,
            tranferInEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThat(transferInResponse.getStatusCodeValue()).isEqualTo(200);

        final var transferredPrisonerResponse = testRestTemplate.exchange(
            "/api/prisoners",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(transferredPrisonerResponse, 200, "transferred_in_prisoner.json");
    }

    @Test
    public void testCannotReleasePrisonerAlreadyOut() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of("movementReasonCode", "CR", "commentText", "released prisoner today");

        final var entity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/release",
            PUT,
            entity,
            ErrorResponse.class,
            "Z0020ZZ"
        );

        final var error = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("Prisoner is not currently active");
    }

    @Test
    public void testCannotTransferInPrisonerNotOut() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var tranferInRequest = Map.of("commentText", "admitted",
            "cellLocation", "MDI-1-3-022");

        final var tranferInEntity = createHttpEntity(token, tranferInRequest);

        final var transferInResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/transfer-in",
            PUT,
            tranferInEntity,
            ErrorResponse.class,
            OFFENDER_NUMBER
        );

        final var error = transferInResponse.getBody();

        assertThat(transferInResponse.getStatusCodeValue()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("Prisoner is not currently being transferred");
    }

    @Test
    public void listAllOffendersUsesDefaultPaginationParams() {

        ResponseEntity<String> response = listAllOffendersUsingHeaders(Map.of());

        assertThatJsonFileAndStatus(response, 200, "list_all_offenders.json");

        assertThat(response.getHeaders().get("Page-Offset")).containsExactly("0");
        assertThat(response.getHeaders().get("Page-Limit")).containsExactly("100");
        assertThat(response.getHeaders().get("Total-Records")).containsExactly("52");
    }

    @Test
    public void testCanRetrieveAddresses() {
        final var requestEntity = createHttpEntity(authTokenHelper.getToken(PRISON_API_USER), null, Map.of());

        final var response = testRestTemplate.exchange(
            "/api/offenders/{offenderNumber}/addresses",
            HttpMethod.GET,
            requestEntity,
            new ParameterizedTypeReference<String>() {
            },
            OFFENDER_NUMBER);

        assertThatJsonFileAndStatus(response, 200, "offender-address.json");
    }

    @Test
    public void testCanGenerateNextNomisIdSequence() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CREATE_BOOKING_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/next-sequence",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 200);
    }

    @Test
    public void testFilterAdjudicationsByFindingCode() {
        final var requestEntity = createHttpEntity(authTokenHelper.getToken(PRISON_API_USER), null, Map.of());

        final var response = testRestTemplate.exchange(
            "/api/offenders/{offenderNumber}/adjudications?finding={findingCode}",
            HttpMethod.GET,
            requestEntity,
            new ParameterizedTypeReference<String>() {
            },
            "A1181HH", "NOT_PROVED");

        assertThatJsonFileAndStatus(response, 200, "adjudications_by_finding_code.json");
    }

    @Test
    public void testInvalidMovedCellSubType() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var newCaseNote = Map.of(
            "type", "MOVED_CELL",
            "subType", "BEH1",
            "text", "This is a test comment"
        );

        final var httpEntity = createHttpEntity(token, newCaseNote);

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/case-notes",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            OFFENDER_NUMBER);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCreateMovedCellCaseNote() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var newCaseNote = Map.of(
            "type", "MOVED_CELL",
            "subType", "BEH",
            "text", "This is a test comment"
        );

        final var httpEntity = createHttpEntity(token, newCaseNote);

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/case-notes",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            "A9876RS");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    private ResponseEntity<String> listAllOffendersUsingHeaders(final Map<String, String> headers) {
        final var requestEntity = createHttpEntity(authTokenHelper.getToken(PRISON_API_USER), null, headers);
        return testRestTemplate.exchange("/api/offenders/ids", HttpMethod.GET, requestEntity, String.class);
    }
}
