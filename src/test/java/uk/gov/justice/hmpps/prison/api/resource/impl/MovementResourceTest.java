package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static java.lang.String.format;
import static java.util.Map.entry;
import static java.util.Map.of;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class MovementResourceTest extends ResourceTest {

    @Test
    public void testReadTodaysMovementsForbidden() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/movements?fromDateTime={fromDateTime}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        assertThatStatus(response, 403);
    }

    @Test
    public void testReadTodaysMovements() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.GLOBAL_SEARCH);

        final var response = testRestTemplate.exchange(
            "/api/movements?fromDateTime={fromDateTime}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        assertThatStatus(response, 200);
        assertThatJson(response.getBody()).isEqualTo("[]");
    }

    @Test
    public void testGetMovementsForOffenders() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.GLOBAL_SEARCH);

        final var body = format("[ \"%s\" ]", "A1179MT");

        final var response = testRestTemplate.exchange(
            "/api/movements/offenders?allBookings=true&latestOnly=false",
            HttpMethod.POST,
            createHttpEntity(token, body),
            new ParameterizedTypeReference<String>() {
            }
        );

        assertThatStatus(response, 200);
        assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("movements_all_bookings.json");
    }

    @Test
    public void testGetMovementsForDateRange() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.GLOBAL_SEARCH);

        final var response = testRestTemplate.exchange(
            "/api/movements?fromDateTime={fromDateTime}&movementDate={movementDate}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            LocalDateTime.of(2018, 4, 25, 0, 0, 0).truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            LocalDate.of(2018, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        );

        assertThatStatus(response, 200);
        assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("movements_on_day.json");
    }

    @Test
    public void testReadRollcountByAgency() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/movements/rollcount/{agencyId}/movements",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            }, "LEI"
        );

        assertThatStatus(response, 200);
        assertThatJson(response.getBody()).isEqualTo("{\"in\":0,\"out\":0}");
    }

    @Test
    public void testReadTodaysMovementsByAgencyEnRoute() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/movements/{agencyId}/enroute",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            }, "LEI"
        );

        assertThatStatus(response, 200);
        assertThatJson(response.getBody()).isEqualTo("[{\"offenderNo\":\"A1183SH\",\"bookingId\":-44,\"dateOfBirth\":\"1980-01-02\",\"firstName\":\"SAM\",\"lastName\":\"HEMP\",\"fromAgency\":\"BMI\",\"fromAgencyDescription\":\"Birmingham\",\"toAgency\":\"LEI\",\"toAgencyDescription\":\"Leeds\",\"movementType\":\"TRN\",\"movementTypeDescription\":\"Transfers\",\"movementReason\":\"NOTR\",\"movementReasonDescription\":\"Normal Transfer\",\"directionCode\":\"OUT\",\"movementTime\":\"13:00:00\",\"movementDate\":\"2017-10-12\"},{\"offenderNo\":\"A1183AD\",\"bookingId\":-45,\"dateOfBirth\":\"1980-01-02\",\"firstName\":\"AMY\",\"lastName\":\"DENTON\",\"fromAgency\":\"BMI\",\"fromAgencyDescription\":\"Birmingham\",\"toAgency\":\"LEI\",\"toAgencyDescription\":\"Leeds\",\"movementType\":\"TRN\",\"movementTypeDescription\":\"Transfers\",\"movementReason\":\"NOTR\",\"movementReasonDescription\":\"Normal Transfer\",\"directionCode\":\"OUT\",\"movementTime\":\"15:00:00\",\"movementDate\":\"2017-10-12\"}]");
    }

    @Test
    public void testGetRolllcountByAgencyEnroute() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/movements/rollcount/{agencyId}/enroute",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            }, "LEI"

        );

        assertThatStatus(response, 200);
        assertThatJson(response.getBody()).isEqualTo("2");
    }

    @Test
    public void testGetMovementsSince() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            }, "LEI", LocalDateTime.of(2019, 1, 1, 0, 1)
        );

        assertThatStatus(response, HttpStatus.OK.value());
        assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("movements_since.json");
    }

    @Test
    public void testGetAllMovementsSince() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}&allMovements=true",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            }, "LEI", LocalDateTime.of(2019, 10, 1, 0, 0)
        );

        assertThatStatus(response, HttpStatus.OK.value());
        assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("movements_since_all.json");
    }

    @Test
    public void testGetMovementsPagination() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}",
            HttpMethod.GET,
            createHttpEntity(token, null, of("Page-Offset", "1", "Page-Limit", "1")),
            new ParameterizedTypeReference<String>() {
            }, "LEI", LocalDateTime.of(2019, 1, 1, 0, 1)
        );

        assertThatStatus(response, HttpStatus.OK.value());
        assertThat(response.getHeaders().toSingleValueMap()).contains(
            entry("Page-Limit", "1"),
            entry("Page-Offset", "1"),
            entry("Total-Records", "2"));

        assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("movements_paged.json");
    }

    @Test
    public void testGetMovementsBetween() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/movements/{agencyId}/in?fromDateTime={fromDateTime}&toDateTime={toDateTime}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            }, "LEI",
            LocalDateTime.of(2019, 4, 1, 0, 1),
            LocalDateTime.of(2019, 6, 1, 0, 1)
        );

        assertThatStatus(response, HttpStatus.OK.value());
        assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("movements_between.json");
    }

    @Test
    public void testGetUpcomingCourtAppearances() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var response = testRestTemplate.exchange(
            "/api/movements/upcomingCourtAppearances",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            }
        );

        assertThatStatus(response, HttpStatus.OK.value());
        assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("movements_upcoming_court.json");
    }

    @Test
    public void testGetAllMovementsOutForAGivenDate() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var movementsOutOnDayResponse = testRestTemplate.exchange(
            "/api/movements/{agencyId}/out/{isoDate}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            }, "LEI", LocalDate.of(2012, 7, 16)
        );

        assertThatStatus(movementsOutOnDayResponse, HttpStatus.OK.value());
        assertThat(getBodyAsJsonContent(movementsOutOnDayResponse)).isStrictlyEqualToJson("movements_out_on_given_day.json");
    }

    @Test
    public void testGetAllMovementsOutForAGivenDateAndMovementType() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var temporaryAbsenceMovementOnDayResponse = testRestTemplate.exchange(
            "/api/movements/{agencyId}/out/{isoDate}?movementType={movementType}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            }, "LEI", LocalDate.of(2012, 7, 16), "tap"
        );

        assertThatStatus(temporaryAbsenceMovementOnDayResponse, HttpStatus.OK.value());
        assertThat(getBodyAsJsonContent(temporaryAbsenceMovementOnDayResponse)).isStrictlyEqualToJson("movements_out_on_given_day_by_type.json");

        final var noCourtMovementsOnDayResponse = testRestTemplate.exchange(
            "/api/movements/{agencyId}/out/{isoDate}?movementType={movementType}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            }, "LEI", LocalDate.of(2017, 7, 16), "CRT"
        );

        assertThatStatus(noCourtMovementsOnDayResponse, HttpStatus.OK.value());
        assertThat(noCourtMovementsOnDayResponse.getBody()).isEqualTo("[]");
    }

    @Nested
    public class ScheduledMovements {
        @Test
        public void getCourtEvents() {
            final var response = getScheduledMovements(true, false, false);

            assertThatStatus(response, HttpStatus.OK.value());

            assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("get_court_events.json");
        }

        @Test
        public void getReleaseEvents() {
            final var fromDateTime = LocalDate.of(2018, 4, 23).atStartOfDay();
            final var toDateTime = LocalDate.of(2018, 4, 23).atTime(20, 10);

            final var response = getScheduledMovements(false, true, false, fromDateTime, toDateTime);

            assertThatStatus(response, HttpStatus.OK.value());

            assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("get_release_events.json");
        }

        @Test
        public void getTransferEvents() {
            final var response = getScheduledMovements(false, false, true);

            assertThatStatus(response, HttpStatus.OK.value());

            assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("get_transfer_events.json");
        }

        private ResponseEntity<String> getScheduledMovements(final Boolean courtEvents, final Boolean releaseEvents, final Boolean transferEvents) {
            final LocalDateTime fromDateTime = LocalDate.of(2020, 1, 1).atTime(9, 0);
            final LocalDateTime toDateTime = LocalDate.of(2020, 1, 1).atTime(12, 0);

            return getScheduledMovements(courtEvents, releaseEvents, transferEvents, fromDateTime, toDateTime);
        }

        @Test
        public void testGetOffendersOutOnTemporaryAbsence() {
            final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

            final var response = testRestTemplate.exchange(
                "/api/movements/agency/{agencyId}/temporary-absences",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<String>() {
                }, "LEI"
            );

            assertThatStatus(response, HttpStatus.OK.value());
            assertThat(getBodyAsJsonContent(response)).isStrictlyEqualToJson("movements_temporary_absence.json");
        }

        private ResponseEntity<String> getScheduledMovements(
            final Boolean courtEvents, final Boolean releaseEvents, final Boolean transferEvents, final LocalDateTime fromDateTime, final LocalDateTime toDateTime) {
            final String token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

            return testRestTemplate.exchange(
                UriComponentsBuilder
                    .fromPath("/api/movements/transfers")
                    .queryParam("agencyId", "LEI")
                    .queryParam("fromDateTime", fromDateTime)
                    .queryParam("toDateTime", toDateTime)
                    .queryParam("courtEvents", courtEvents)
                    .queryParam("releaseEvents", releaseEvents)
                    .queryParam("transferEvents", transferEvents)
                    .build()
                    .toUriString(),
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<>() {
                });
        }
    }
}
