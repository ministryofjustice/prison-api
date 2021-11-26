package uk.gov.justice.hmpps.prison.api.resource.impl;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.PRISON_API_USER;

@ContextConfiguration(classes = OffendersResourceTest.TestClock.class)
public class OffendersResourceTest extends ResourceTest {

    @TestConfiguration
    static class TestClock {
        private final LocalDateTime timeIs_2020_10_01T000000 = LocalDate.parse("2020-10-01", DateTimeFormatter.ISO_DATE).atStartOfDay();

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
            GET,
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
            GET,
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
            "/api/offenders/{nomsId}/alerts/v2",
            GET,
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
            "/api/offenders/{nomsId}/case-notes/v2",
            GET,
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
            "/api/offenders/{nomsId}/case-notes/v2",
            GET,
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
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            OFFENDER_NUMBER);

        assertThatJsonFileAndStatus(response, 200, "offender_detail.json");
    }

    @Test
    public void compareV1AndV1_1VersionsOfGetOffender() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

        final var httpEntityV1 = createHttpEntity(token, null, Map.of("version", "1.0"));

        final var responseV1 = testRestTemplate.exchange(
            "/api/offenders/{nomsId}",
            GET,
            httpEntityV1,
            new ParameterizedTypeReference<String>() {
            },
            OFFENDER_NUMBER);

        assertThatJsonFileAndStatus(responseV1, 200, "offender_detail_v1.1.json");

        final var httpEntityV1_1 = createHttpEntity(token, null, Map.of("version", "1.1_beta"));

        final var responseV1_1 = testRestTemplate.exchange(
            "/api/offenders/{nomsId}",
            GET,
            httpEntityV1_1,
            new ParameterizedTypeReference<String>() {
            },
            OFFENDER_NUMBER);

        assertThatJsonFileAndStatus(responseV1_1, 200, "offender_detail_v1.1.json");
    }

    @Test
    public void testOffenderWithActiveRecallOffence() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}",
            GET,
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
            GET,
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
            GET,
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
            GET,
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
            GET,
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
            GET,
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
            GET,
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
            GET,
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
            GET,
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
            GET,
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
            GET,
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
            GET,
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
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testViewPrisonTimeline() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null);
        final var prisonerNo = "A1234AA";
        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/prison-timeline",
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo);

        assertThatJsonFileAndStatus(response, 200, "prisoner_timeline.json");
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
    public void testCanCreateANewPrisoner() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of(
            "pncNumber", "03/11999M",
            "lastName", "d'Arras",
            "firstName", "Mathias",
            "middleName1", "Hector",
            "middleName2", "Sausage-Hausen",
            "title", "MR",
            "croNumber", "D827492834",
            "dateOfBirth", LocalDate.of(2000, 10, 17).format(DateTimeFormatter.ISO_LOCAL_DATE),
            "gender", "M",
            "ethnicity", "M1");

        final var entity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders",
            POST,
            entity,
            new ParameterizedTypeReference<String>() {
            }
        );

        assertThatJsonFileAndStatus(response, 200, "new_prisoner.json");
    }

    @Test
    public void testCanMovePrisonerFromCourtToHospital() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of(
            "lastName", "TestSurnam",
            "firstName", "TestFirstnam",
            "dateOfBirth", LocalDate.of(2001, 10, 19).format(DateTimeFormatter.ISO_LOCAL_DATE),
            "gender", "M",
            "ethnicity", "W1");

        final var entity = createHttpEntity(token, body);

        final var createResponse =  testRestTemplate.exchange(
            "/api/offenders",
            POST,
            entity,
            new ParameterizedTypeReference<String>() {
            }
        );

        final var offenderNo = new Gson().fromJson(createResponse.getBody(), Map.class).get("offenderNo");

        final var dischargeRequest = Map.of(
            "hospitalLocationCode", "ARNOLD",
            "dischargeTime", LocalDateTime.of(2021, 5, 18, 17, 23, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "commentText", "Discharged to Psychiatric hospital",
            "supportingPrisonId", "LEI",
            "fromLocationId", "COURT1");
        final var dischargeEntity = createHttpEntity(token, dischargeRequest);

        final var dischargeResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/discharge-to-hospital",
            PUT,
            dischargeEntity,
            new ParameterizedTypeReference<String>() {
            },
            offenderNo
        );
        assertThatJsonFileAndStatus(dischargeResponse, 200, "discharged_from_court.json");
    }

    @Test
    public void testCanMovePrisonerFromPrisonToHospital() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of(
            "lastName", "TestSurname",
            "firstName", "TestFirstname",
            "dateOfBirth", LocalDate.of(2000, 10, 17).format(DateTimeFormatter.ISO_LOCAL_DATE),
            "gender", "M",
            "ethnicity", "M1");

        final var entity = createHttpEntity(token, body);

        final var createResponse =  testRestTemplate.exchange(
            "/api/offenders",
            POST,
            entity,
            new ParameterizedTypeReference<String>() {
            }
        );

        final var offenderNo = new Gson().fromJson(createResponse.getBody(), Map.class).get("offenderNo");

        final var newBookingBody = Map.of("prisonId", "SYI", "fromLocationId", "COURT1", "movementReasonCode", "24", "youthOffender", "true", "imprisonmentStatus", "CUR_ORA", "cellLocation", "SYI-A-1-1");
        final var newBookingEntity = createHttpEntity(token, newBookingBody);

        final var newBookingResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/booking",
            POST,
            newBookingEntity,
            new ParameterizedTypeReference<String>() {
            },
            offenderNo
        );
        assertThat(newBookingResponse.getStatusCodeValue()).isEqualTo(200);

        final var bookingId = new BigDecimal(new Gson().fromJson(newBookingResponse.getBody(), Map.class).get("bookingId").toString()).toBigInteger().longValue();
        final var releaseBody = createHttpEntity(token, Map.of("movementReasonCode", "CR", "commentText", "released prisoner incorrectly"));

        final var releaseResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/release",
            PUT,
            releaseBody,
            new ParameterizedTypeReference<String>() {
            },
            offenderNo
        );

        assertThat(releaseResponse.getStatusCodeValue()).isEqualTo(200);

        // check that no new movement is created
        final var movementCheck1 =  testRestTemplate.exchange(
            "/api/bookings/{bookingId}/movement/{sequenceNumber}",
            GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            bookingId, 2
        );

        assertThatJsonFileAndStatus(movementCheck1, 200, "movement_discharge_1.json");

        final var dischargeRequest = Map.of(
            "hospitalLocationCode", "HAZLWD",
            "dischargeTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "commentText", "Discharged to Psychiatric hospital",
            "supportingPrisonId", "LEI",
            "fromLocationId", "COURT1");
        final var dischargeEntity = createHttpEntity(token, dischargeRequest);

        final var dischargeResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/discharge-to-hospital",
            PUT,
            dischargeEntity,
            new ParameterizedTypeReference<String>() {
            },
            offenderNo
        );
        assertThatJsonFileAndStatus(dischargeResponse, 200, "discharged_from_prison.json");

        // check that no new movement is created
        final var movementCheck2 =  testRestTemplate.exchange(
            "/api/bookings/{bookingId}/movement/{sequenceNumber}",
            GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            bookingId, 2
        );

        assertThatJsonFileAndStatus(movementCheck2, 200, "movement_discharge_2.json");

        final var response = testRestTemplate.exchange(
            "/api/offenders/{nomsId}",
            GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            offenderNo);

        assertThatOKResponseContainsJson(response, """
              {
                  "locationDescription": "Outside - released from LEEDS",
                  "latestLocationId": "LEI"
              }
            """);

    }

    @Test
    public void testCanCreateANewPrisonerAndBooking() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of(
            "pncNumber", "03/11964Z",
            "lastName", "Janus",
            "firstName", "Jayne",
            "middleName1", "Sarah",
            "middleName2", "Mia",
            "title", "MRS",
            "dateOfBirth", LocalDate.of(2001, 10, 30).format(DateTimeFormatter.ISO_LOCAL_DATE),
            "gender", "F",
            "ethnicity", "W1");

        final var entity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders",
            POST,
            entity,
            new ParameterizedTypeReference<String>() {
            }
        );

        final var offenderNo = new Gson().fromJson(response.getBody(), Map.class).get("offenderNo");

        final var newBookingBody = Map.of("prisonId", "SYI", "fromLocationId", "COURT1", "movementReasonCode", "24", "youthOffender", "true", "imprisonmentStatus", "CUR_ORA", "cellLocation", "SYI-A-1-1");
        final var newBookingEntity = createHttpEntity(token, newBookingBody);

        final var newBookingResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/booking",
            POST,
            newBookingEntity,
            new ParameterizedTypeReference<String>() {
            },
            offenderNo
        );
        assertThatJsonFileAndStatus(newBookingResponse, 200, "new_offender_and_booking.json");

        final var releaseBody = createHttpEntity(token, Map.of("movementReasonCode", "CR", "commentText", "released prisoner today"));

        final var releaseResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/release",
            PUT,
            releaseBody,
            new ParameterizedTypeReference<String>() {
            },
            offenderNo
        );

        assertThat(releaseResponse.getStatusCodeValue()).isEqualTo(200);

    }

    @Test
    public void testNewBookingRejectedIfCellAtCapacity() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of(
            "pncNumber", "03/11965Z",
            "lastName", "BookedInto",
            "firstName", "FullCell",
            "title", "MR",
            "dateOfBirth", LocalDate.of(2001, 10, 30).format(DateTimeFormatter.ISO_LOCAL_DATE),
            "gender", "M",
            "ethnicity", "W1");

        final var entity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders",
            POST,
            entity,
            new ParameterizedTypeReference<String>() {
            }
        );

        final var offenderNo = new Gson().fromJson(response.getBody(), Map.class).get("offenderNo");

        final var newBookingBody = Map.of("prisonId", "MDI", "fromLocationId", "COURT1", "movementReasonCode", "24", "youthOffender", "false", "imprisonmentStatus", "CUR_ORA", "cellLocation", "MDI-FULL");
        final var newBookingEntity = createHttpEntity(token, newBookingBody);

        final var newBookingResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/booking",
            POST,
            newBookingEntity,
            new ParameterizedTypeReference<String>() {
            },
            offenderNo
        );
        assertThatStatus(newBookingResponse, 409);
    }


    @Test
    public void testCannotCreateNewPrisonerWithExistingPNC() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of(
            "pncNumber", "98/1234567L",
            "lastName", "d'Arras",
            "firstName", "Mathias",
            "middleName1", "Hector",
            "middleName2", "Sausage-Hausen",
            "title", "MR",
            "suffix", "JR",
            "dateOfBirth", LocalDate.of(2000, 10, 17).format(DateTimeFormatter.ISO_LOCAL_DATE),
            "gender", "M",
            "ethnicity", "M1");

        final var entity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders",
            POST,
            entity,
            ErrorResponse.class
        );

        final var error = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("Prisoner with PNC 1998/1234567L already exists with ID A1234AD");
    }

    @Test
    public void testCannotCreateNewPrisonerWithExistingCRO() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of(
            "croNumber", "CRO112234",
            "lastName", "d'Arras",
            "firstName", "Mathias",
            "middleName1", "Hector",
            "middleName2", "Sausage-Hausen",
            "title", "MR",
            "suffix", "JR",
            "dateOfBirth", LocalDate.of(2000, 10, 17).format(DateTimeFormatter.ISO_LOCAL_DATE),
            "gender", "M",
            "ethnicity", "M1");

        final var entity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders",
            POST,
            entity,
            ErrorResponse.class
        );

        final var error = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("Prisoner with CRO CRO112234 already exists with ID A1234AC");
    }


    @Test
    public void testCannotCreateNewPrisonerWithSameNames() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of(
            "lastName", "Andrews",
            "firstName", "Anthony",
            "dateOfBirth", LocalDate.of(1964, 12, 1).format(DateTimeFormatter.ISO_LOCAL_DATE),
            "gender", "M");

        final var entity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders",
            POST,
            entity,
            ErrorResponse.class
        );

        final var error = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("Prisoner with lastname ANDREWS, firstname ANTHONY and dob 1964-12-01 already exists with ID A1234AF");
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

        assertThatJsonFileAndStatus(response, 200, "released_prisoner.json");
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

        assertThatJsonFileAndStatus(response, 200, "transferred_out_prisoner.json");

        final var tranferInRequest = Map.of("receiveTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "commentText", "admitted",
            "cellLocation", "MDI-1-3-022");

        final var transferInEntity = createHttpEntity(token, tranferInRequest);

        final var transferInResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/transfer-in",
            PUT,
            transferInEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThatJsonFileAndStatus(transferInResponse, 200, "transferred_in_prisoner.json");
    }

    @Test
    public void testReturnFromCourt() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var now = LocalDateTime.now();

        final var prisonerNo = "A1180HI";


        final var courtReturnRequest = Map.of("agencyId","MDI",
            "movementReasonCode","CA",
            "commentText", "admitted",
            "dateTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

        final var courtReturnEntity = createHttpEntity(token, courtReturnRequest);

        final var transferInResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/court-return",
            PUT,
            courtReturnEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThatJsonFileAndStatus(transferInResponse, 200, "return_from_court.json");
    }


    @Test
    public void testCannotTransferAPrisonerIntoLocationAtCapacity() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var now = LocalDateTime.now();
        final var body = Map.of("transferReasonCode", "NOTR", "commentText", "transferred prisoner today", "toLocation", "MDI",
            "movementTime", now.minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        final var entity = createHttpEntity(token, body);

        final var prisonerNo = "A1180HJ";
        final var response =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/transfer-out",
            PUT,
            entity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThatStatus(response, 200);

        final var failingTranferInRequest = Map.of("receiveTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "commentText", "admitted",
            "cellLocation", "MDI-FULL");

        final var failingTransferInEntity = createHttpEntity(token, failingTranferInRequest);

        final var failingTransferInResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/transfer-in",
            PUT,
            failingTransferInEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThatStatus(failingTransferInResponse, 409);

        final var tranferInRequest = Map.of("receiveTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "commentText", "admitted",
            "cellLocation", "MDI-1-3-022");

        final var transferInEntity = createHttpEntity(token, tranferInRequest);

        final var transferInResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/transfer-in",
            PUT,
            transferInEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThatStatus(transferInResponse, 200);
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
    public void testRecallAPrisoner() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of("prisonId", "SYI", "fromLocationId", "COURT1", "movementReasonCode", "24", "youthOffender", "true", "imprisonmentStatus", "CUR_ORA", "cellLocation", "SYI-A-1-1");

        final var recallEntity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/recall",
            PUT,
            recallEntity,
            new ParameterizedTypeReference<String>() {
            },
            "Z0022ZZ"
        );

        assertThatJsonFileAndStatus(response, 200, "recalled_prisoner.json");

        final var releaseBody = createHttpEntity(token, Map.of("movementReasonCode", "CR", "commentText", "released prisoner today"));

        final var releaseResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/release",
            PUT,
            releaseBody,
            new ParameterizedTypeReference<String>() {
            },
            "Z0022ZZ"
        );

        assertThat(releaseResponse.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void testRecallAPrisonerFailsIfLocationAtCapacity() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of("prisonId", "MDI", "fromLocationId", "COURT1", "movementReasonCode", "24", "youthOffender", "true", "imprisonmentStatus", "CUR_ORA", "cellLocation", "MDI-FULL");

        final var recallEntity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/recall",
            PUT,
            recallEntity,
            new ParameterizedTypeReference<String>() {
            },
            "Z0022ZZ"
        );

        assertThatStatus(response, 409);
    }

    @Test
    public void testReceiveAPrisonerAsNewBooking() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of("prisonId", "SYI", "fromLocationId", "COURT1", "movementReasonCode", "24", "youthOffender", "true", "imprisonmentStatus", "CUR_ORA", "cellLocation", "SYI-A-1-1");

        final var newBookingEntity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/booking",
            POST,
            newBookingEntity,
            new ParameterizedTypeReference<String>() {
            },
            "Z0022ZZ"
        );
        assertThatJsonFileAndStatus(response, 200, "new_booking_prisoner.json");

        final var releaseBody = createHttpEntity(token, Map.of("movementReasonCode", "CR", "commentText", "released prisoner today"));

        final var releaseResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/release",
            PUT,
            releaseBody,
            new ParameterizedTypeReference<String>() {
            },
            "Z0022ZZ"
        );

        assertThat(releaseResponse.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void testReceiveAPrisonerAsNewBookingForFirstTime() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var body = Map.of("prisonId", "SYI", "fromLocationId", "COURT1", "movementReasonCode", "24", "youthOffender", "true", "imprisonmentStatus", "CUR_ORA", "cellLocation", "SYI-A-1-1");

        final var newBookingEntity = createHttpEntity(token, body);

        final var response =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/booking",
            POST,
            newBookingEntity,
            new ParameterizedTypeReference<String>() {
            },
            "A9880GH"
        );
        assertThatJsonFileAndStatus(response, 200, "first_new_booking_prisoner.json");

        final var releaseBody = createHttpEntity(token, Map.of("movementReasonCode", "CR", "commentText", "released prisoner today"));

        final var releaseResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/release",
            PUT,
            releaseBody,
            new ParameterizedTypeReference<String>() {
            },
            "A9880GH"
        );

        assertThat(releaseResponse.getStatusCodeValue()).isEqualTo(200);
    }


    @Test
    public void testCannotTransferInPrisonerNotOut() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);

        final var tranferInRequest = Map.of("commentText", "admitted",
            "cellLocation", "MDI-1-3-022");

        final var transferInEntity = createHttpEntity(token, tranferInRequest);

        final var transferInResponse =  testRestTemplate.exchange(
            "/api/offenders/{nomsId}/transfer-in",
            PUT,
            transferInEntity,
            ErrorResponse.class,
            OFFENDER_NUMBER
        );

        final var error = transferInResponse.getBody();

        assertThat(transferInResponse.getStatusCodeValue()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("Prisoner is not currently being transferred");
    }

    @Test
    public void testCanRetrieveAddresses() {
        final var requestEntity = createHttpEntity(authTokenHelper.getToken(PRISON_API_USER), null, Map.of());

        final var response = testRestTemplate.exchange(
            "/api/offenders/{offenderNumber}/addresses",
            GET,
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
            GET,
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
            GET,
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
            POST,
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
            POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            "A9876RS");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    private ResponseEntity<String> listAllOffendersUsingHeaders(final Map<String, String> headers) {
        final var requestEntity = createHttpEntity(authTokenHelper.getToken(PRISON_API_USER), null, headers);
        return testRestTemplate.exchange("/api/offenders/ids", GET, requestEntity, String.class);
    }
}
