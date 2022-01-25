package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.AlertChanges;
import uk.gov.justice.hmpps.prison.api.model.AlertCreated;
import uk.gov.justice.hmpps.prison.api.model.BookingActivity;
import uk.gov.justice.hmpps.prison.api.model.CreateAlert;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetails;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendanceBatch;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@ContextConfiguration(classes = BookingResourceIntTest.TestClock.class)
public class BookingResourceIntTest extends ResourceTest {

    @TestConfiguration
    static class TestClock {
        @Bean
        public Clock clock() {
            return Clock.fixed(
                LocalDateTime.of(2020, 1, 2, 3, 4, 5).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        }
    }

    @Test
    public void testGetBooking() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            -2);
        assertThatJsonFileAndStatus(response, 200, "booking_offender_-1.json");
    }

    @Test
    public void testGetBookingsV2ByPrison() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/bookings/v2?prisonId={prisonId}&sort={sort}&image={imageRequired}&iepLevel={iepLevel}&legalInfo={legalInfo}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            Map.of("prisonId", "BXI", "sort", "bookingId,asc", "imageRequired", "true", "iepLevel", "false", "legalInfo", "true"));
        assertThatJsonFileAndStatus(response, 200, "bxi_caseload_bookings.json");
    }

    @Test
    public void testGetBookingsV2ByPrisonPaginated() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/bookings/v2?prisonId={prisonId}&page={pageNum}&size={pageSize}&image={imageRequired}&iepLevel={iepLevel}&legalInfo={legalInfo}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            Map.of("prisonId", "LEI", "pageNum", "2", "pageSize", "3", "imageRequired", "true", "iepLevel", "true", "legalInfo", "true"));
        assertThatJsonFileAndStatus(response, 200, "lei_bookings.json");
    }

    @Test
    public void testGetBookingsV2ByBookingId() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/bookings/v2?bookingId={bookingId1}&bookingId={bookingId2}&bookingId={bookingId3}&image={imageRequired}&iepLevel={iepLevel}&legalInfo={legalInfo}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            Map.of("bookingId1", "-1", "bookingId2", "-2", "bookingId3", "-3", "imageRequired", "true", "iepLevel", "false", "legalInfo", "true"));
        assertThatJsonFileAndStatus(response, 200, "bookings_by_id.json");
    }

    @Test
    public void testGetBookingsV2ByOffenderNo() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/bookings/v2?offenderNo={nomsId1}&offenderNo={nomsId2}&image={imageRequired}&iepLevel={iepLevel}&legalInfo={legalInfo}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            Map.of("nomsId1", "A1234AA", "nomsId2", "A1234AB", "imageRequired", "true", "iepLevel", "true", "legalInfo", "true"));
        assertThatJsonFileAndStatus(response, 200, "bookings_by_nomsId.json");
    }

    @Test
    public void testThatUpdateAttendanceIsLockedDown_WhenPayRoleIsMissing() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var body = Map.of("eventOutcome", "ATT", "performance", "STANDARD");
        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/activities/{activityId}/attendance",
            HttpMethod.PUT,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            -2, -11);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testUpdateAttendance_WithTheValidRole() {
        final var token = authTokenHelper.getToken(AuthToken.PAY);

        final var body = Map.of("eventOutcome", "ATT", "performance", "STANDARD");
        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/activities/{activityId}/attendance",
            HttpMethod.PUT,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            -2, -11);

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void testUpdateAttendance_WithInvalidBookingId() {
        final var token = authTokenHelper.getToken(AuthToken.PAY);
        final var body = Map.of("eventOutcome", "ATT", "performance", "STANDARD");
        final var request = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/activities/{activityId}/attendance",
            HttpMethod.PUT,
            request,
            ErrorResponse.class, 0, -11);

        assertThat(response.getBody()).isEqualTo(
            ErrorResponse.builder()
                .status(404)
                .userMessage("Resource with id [0] not found.")
                .developerMessage("Resource with id [0] not found.")
                .build());
    }

    @Test
    public void testUpdateAttendance_WithMultipleBookingIds() {
        final var token = authTokenHelper.getToken(AuthToken.PAY);

        final var body = UpdateAttendanceBatch
            .builder()
            .eventOutcome("ATT")
            .performance("STANDARD")
            .bookingActivities(Set.of(BookingActivity.builder().activityId(-11L).bookingId(-2L).build()))
            .build();

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/bookings/activities/attendance",
            HttpMethod.PUT,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void testCreateNewAlert_UnAuthorised() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var body = CreateAlert.builder().alertCode("X").alertType("XX").comment("XXX")
            .alertDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/alert",
            HttpMethod.POST,
            createHttpEntity(token, body),
            new ParameterizedTypeReference<ErrorResponse>() {
            }, -10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testUpdateAlert_UnAuthorised() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var body = AlertChanges.builder().expiryDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/alert/{alertSeq}",
            HttpMethod.PUT,
            createHttpEntity(token, body),
            new ParameterizedTypeReference<ErrorResponse>() {
            }, -1L, 4);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testUpdateAlert() {
        final var token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT);

        final var createdAlert = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/alert",
            HttpMethod.POST,
            createHttpEntity(token,
                CreateAlert.builder()
                    .alertType("L")
                    .alertCode("LPQAA")
                    .comment("XXX")
                    .alertDate(LocalDate.now())
                    .build()),
            new ParameterizedTypeReference<Alert>() {
            }, -14L).getBody();

        final var body = AlertChanges.builder().expiryDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/alert/{alertSeq}",
            HttpMethod.PUT,
            createHttpEntity(token, body),
            new ParameterizedTypeReference<AlertCreated>() {
            }, -14L, createdAlert.getAlertId());

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void testUpdateAlert_CommentTextOnly() {
        final var token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT);

        final var createdAlert = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/alert",
            HttpMethod.POST,
            createHttpEntity(token,
                CreateAlert.builder()
                    .alertType("L")
                    .alertCode("LPQAA")
                    .comment("XXX")
                    .alertDate(LocalDate.now())
                    .build()),
            new ParameterizedTypeReference<Alert>() {
            }, -14L).getBody();

        final var body = AlertChanges.builder().comment("New comment").build();

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/alert/{alertSeq}",
            HttpMethod.PUT,
            createHttpEntity(token, body),
            new ParameterizedTypeReference<AlertCreated>() {
            }, -14L, createdAlert.getAlertId());

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void testCreateNewAlert_BadRequest() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var body = CreateAlert.builder().build();

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/alert",
            HttpMethod.POST,
            createHttpEntity(token, body),
            new ParameterizedTypeReference<ErrorResponse>() {
            }, -10L);

        final var validationMessages = response.getBody().getUserMessage();

        assertThat(validationMessages).contains("alertType");
        assertThat(validationMessages).contains("alertCode");
        assertThat(validationMessages).contains("comment");
        assertThat(validationMessages).contains("alertDate");
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void testCreateNewAlert_MaximumLengths() {
        final var token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT);
        final var largeText = IntStream.range(1, 1002).mapToObj(i -> "A").collect(Collectors.joining(""));

        final var body = CreateAlert.builder()
            .alertCode(largeText.substring(0, 13))
            .alertType(largeText.substring(0, 13))
            .comment(largeText)
            .alertDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/alert",
            HttpMethod.POST,
            createHttpEntity(token, body),
            new ParameterizedTypeReference<ErrorResponse>() {
            }, -10L);

        final var validationMessages = response.getBody().getUserMessage();

        assertThat(validationMessages).contains("alertType");
        assertThat(validationMessages).contains("alertCode");
        assertThat(validationMessages).contains("comment");
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }


    @Test
    public void testCreateNewAlert() {
        final var token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT);

        final var body = CreateAlert.builder().alertType("L").alertCode("LPQAA").comment("comments")
            .alertDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/alert",
            HttpMethod.POST,
            createHttpEntity(token, body),
            new ParameterizedTypeReference<AlertCreated>() {
            }, -10L);

        assertThat(response.getBody().getAlertId()).isGreaterThan(1);
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void testGetBasicInmateDetailsForOffendersActiveOnlyFalse() {
        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);

        final var body = List.of("Z0020ZZ");

        final var response = testRestTemplate.exchange(
            "/api/bookings/offenders?activeOnly=false",
            HttpMethod.POST,
            createHttpEntity(token, body),
            new ParameterizedTypeReference<List<InmateBasicDetails>>() {
            });

        assertThat(response.getBody().get(0).getBookingId()).isEqualTo(-20);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void testGetMovementForBooking() {
        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/movement/{sequenceNumber}",
            GET,
            createHttpEntity(token, null),
            Movement.class, "-29", "2");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().getFromAgency()).isEqualTo("LEI");
        assertThat(response.getBody().getToAgency()).isEqualTo("BMI");
    }

    @Test
    public void testGetMovementForBookingNoResults() {
        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);

        final var response = testRestTemplate.exchange(
            "/api/bookings/{bookingId}/movement/{sequenceNumber}",
            GET,
            createHttpEntity(token, null),
            Movement.class, "-29", "999");

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void getMainOffence_testRetrieveSingleOffence() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/mainOffence", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "-1");

        assertThatJsonFileAndStatus(response, 200, "offender_main_offence.json");
    }

    @Test
    public void getMainOffence_testRetrieveMultipleOffences() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/mainOffence", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "-7");

        assertThatJsonFileAndStatus(response, 200, "offender_main_offences.json");
    }

    @Test
    public void getFullOffenderInformation() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}?extraInfo=true", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "-7");

        assertThatJsonFileAndStatus(response, 200, "offender_extra_info.json");
    }

    @Test
    public void getFullOffenderInformation_byOffenderNo() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "A1234AG");
        assertThatJsonFileAndStatus(response, 200, "offender_extra_info.json");
    }

    @Test
    public void getFullOffenderInformation_byOffenderNoAlt() {
        final var response = testRestTemplate.exchange("/api/offenders/{offenderNo}", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "A1234AG");
        assertThatJsonFileAndStatus(response, 200, "offender_extra_info.json");
    }

    @Test
    public void getFullOffenderInformationNoCSRA_byOffenderNo() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "A1184MA");
        assertThatJsonFileAndStatus(response, 200, "offender_extra_info_no_csra.json");
    }

    @Test
    public void getFullOffenderInformationWithCSRA_byOffenderNo() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}?fullInfo=true&extraInfo=true&csraSummary=true", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "A1184MA");
        assertThatJsonFileAndStatus(response, 200, "offender_extra_info_with_csra.json");
    }

    @Test
    public void getFullOffenderInformationPersonalCare_byOffenderNo() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "A1234AA");
        assertThatJsonFileAndStatus(response, 200, "offender_personal_care.json");
    }

    @Test
    public void getMainOffence_notFound() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/mainOffence", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "-99");

        assertThatStatus(response, 404);
    }

    @Test
    public void getMainOffence_notInCaseload() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/mainOffence", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "-16");

        assertThatStatus(response, 404);
    }

    @Test
    public void getMainOffence_noOffences() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/mainOffence", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, "-9");

        assertThatStatus(response, 200);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    public void getOffenceHistory_post() {
        final var response = testRestTemplate.exchange("/api/bookings/mainOffence", POST,
            createHttpEntity(AuthToken.SYSTEM_USER_READ_WRITE, "[-1, -7]"),
            String.class);

        assertThatJsonFileAndStatus(response, 200, "offender_main_offences_post.json");
    }

    @Test
    public void getOffenceHistory_post_no_offences() {
        final var response = testRestTemplate.exchange("/api/bookings/mainOffence", POST,
            createHttpEntity(AuthToken.SYSTEM_USER_READ_WRITE, "[ -98, -99 ]"),
            String.class);

        assertThatStatus(response, 200);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    public void getOffenceHistory() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}/offenceHistory", GET,
            createHttpEntity(AuthToken.VIEW_PRISONER_DATA, null),
            String.class, "A1234AG");

        assertThatJsonFileAndStatus(response, 200, "offender_main_offences.json");
    }

    @Test
    public void getOffenceHistoryIncludeOffenderWithoutConviction() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}/offenceHistory?convictionsOnly=false", GET,
            createHttpEntity(AuthToken.VIEW_PRISONER_DATA, null),
            String.class, "A1234AB");

        assertThatJsonFileAndStatus(response, 200, "offender_offence_history_A12234AB_include_non_convictions.json");
    }

    @Test
    public void getSecondaryLanguages() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/secondary-languages", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -3L);

        assertThatJsonFileAndStatus(response, 200, "secondary_languages.json");
    }

    @Test
    public void getNextVisit() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits/next", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -3L);

        assertThatJsonFileAndStatus(response, 200, "next-visit.json");
    }

    @Test
    public void getNextVisit_withVisitors() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits/next?withVisitors=true", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -3L);

        assertThatJsonFileAndStatus(response, 200, "next-visit-with-visitors.json");
    }

    @Test
    public void getNextVisit_withVisitors_whenNotPresent() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits/next?withVisitors=true", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -1);

        assertThatStatus(response, 200);
    }

    @Test
    public void getNextVisit_whenNotPresent() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits/next", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -1);

        assertThatStatus(response, 200);
    }

    @Test
    public void getVisitsWithVisitorsWithMissingPageAndSize() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -6L);

        assertThatJsonFileAndStatus(response, 200, "visits_with_visitors.json");
    }

    @Test
    public void getVisitsWithVisitorsWithPageAndSize() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?size=5&page=1", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -6L);

        assertThatJsonFileAndStatus(response, 200, "visits_with_visitors_paged.json");
    }

    @Test
    public void getVisitsWithVisitorsWithPageAndSizeAsMax() {
        var page = Integer.MAX_VALUE;
        var size = Integer.MAX_VALUE;
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?size=" + size + "&page=" + page, GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -6L);

        assertThatStatus(response, 200);
    }

    @Test
    public void getVisitsWithVisitorsWithPageAsMin() {
        var page = Integer.MIN_VALUE;
        var size = 20;
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?size=" + size + "&page=" + page, GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -6L);

        assertThatStatus(response, 400);
        assertThat(response.getBody()).contains("Page index must not be less than zero!");
    }

    @Test
    public void getVisitsWithVisitorsWithSizeAsMin() {
        var page = 0;
        var size = Integer.MIN_VALUE;
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?size=" + size + "&page=" + page, GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -6L);

        assertThatStatus(response, 400);
        assertThat(response.getBody()).contains("Page size must not be less than one!");
    }

    @Test
    public void getVisitsWithVisitorsWithOutOfRangePage() {
        var page = Long.MAX_VALUE;
        var size = 20;
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?size=" + size + "&page=" + page, GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -6L);

        assertThatStatus(response, 400);
        assertThat(response.getBody()).contains("For input string: \\\"9223372036854775807\\\"");
    }

    @Test
    public void getVisitsWithVisitorsWithOutOfRangeSize() {
        var page = 0;
        var size = Long.MAX_VALUE;
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?size=" + size + "&page=" + page, GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -6L);

        assertThatStatus(response, 400);
        assertThat(response.getBody()).contains("For input string: \\\"9223372036854775807\\\"");
    }

    @Test
    public void getVisitsWithVisitorsWithNonNumberPage() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?size=1&page=123x", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -6L);

        assertThatStatus(response, 400);
        assertThat(response.getBody()).contains("For input string: \\\"123x\\\"");
    }

    @Test
    public void getVisitsWithVisitorsWithNonNumberSize() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?size=1x&page=0", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -6L);

        assertThatStatus(response, 400);
        assertThat(response.getBody()).contains("For input string: \\\"1x\\\"");
    }

    @Test
    public void getVisitsWithVisitorsFilteredPagination() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?fromDate=2019-07-15&size=5", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -6L);

        assertThatJsonFileAndStatus(response, 200, "visits_with_visitors_filter_paged.json");
    }

    @Test
    public void getVisitsWithVisitorsWithStatus() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?visitStatus=CANC", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -1L);

        assertThatJsonFileAndStatus(response, 200, "visits_with_visitors_filtered_by_status_paged.json");
    }

    @Test
    public void getVisitsWithVisitorsFilteredByPrison() {
        final var responseAll = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?prisonId=", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -1L);

        assertThat(getBodyAsJsonContent(responseAll)).extractingJsonPathNumberValue("$.numberOfElements").isEqualTo(15);

        final var responseLei = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?prisonId=LEI", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -1L);

        assertThat(getBodyAsJsonContent(responseLei)).extractingJsonPathNumberValue("$.numberOfElements").isEqualTo(13);

        final var responseMdi = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?prisonId=MDI", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -1L);

        assertThat(getBodyAsJsonContent(responseMdi)).extractingJsonPathNumberValue("$.numberOfElements").isEqualTo(1);
    }

    @Test
    public void getVisitsWithVisitorsFilteredByCancellationReason() {
        final var responseAll = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?cancellationReason=", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -1L);

        assertThat(getBodyAsJsonContent(responseAll)).extractingJsonPathNumberValue("$.numberOfElements").isEqualTo(15);

        final var responseLei = testRestTemplate.exchange("/api/bookings/{bookingId}/visits-with-visitors?cancellationReason=NSHOW", GET,
            createHttpEntity(AuthToken.NORMAL_USER, null),
            String.class, -1L);

        assertThat(getBodyAsJsonContent(responseLei)).extractingJsonPathNumberValue("$.numberOfElements").isEqualTo(2);
    }

    @Test
    public void getNonAssociationDetails_victim_rival_gang_and_perpetrator() {
        final var response = testRestTemplate.exchange(
            "/api/bookings/-1/non-association-details",
            HttpMethod.GET,
            createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER), Map.of()),
            String.class);

        assertThatJsonFileAndStatus(response, 200, "offender_non_association_details_vic_rival_gang_and_per.json");
    }

    @Test
    public void getNonAssociationDetails_perpetrator_and_victim() {
        final var response = testRestTemplate.exchange(
            "/api/bookings/-2/non-association-details",
            HttpMethod.GET,
            createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER), Map.of()),
            String.class);

        assertThatJsonFileAndStatus(response, 200, "offender_non_association_details_per_vic.json");
    }

    @Test
    public void getBedAssignmentHistory_with_defaultSorting() {
        final var response = testRestTemplate.exchange(
            "/api/bookings/-36/cell-history",
            HttpMethod.GET,
            createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER), Map.of()),
            String.class);

        final var bodyAsJsonContent = getBodyAsJsonContent(response);
        assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.content[0].bookingId").isEqualTo(-36);
        assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.content[0].livingUnitId").isEqualTo(-18);
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[0].assignmentDate").isEqualTo("2060-10-17");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[0].assignmentDateTime").isEqualTo("2060-10-17T11:00:00");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[0].assignmentReason").isEqualTo("ADM");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[0].agencyId").isEqualTo("LEI");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[0].description").isEqualTo("LEI-H-1-4");
        assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.content[0].bedAssignmentHistorySequence").isEqualTo(4);
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[0].movementMadeBy").isEqualTo("SA");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[0].offenderNo").isEqualTo("A1180MA");

        assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.content[1].bookingId").isEqualTo(-36);
        assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.content[1].livingUnitId").isEqualTo(-17);
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[1].assignmentDate").isEqualTo("2050-10-17");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[1].assignmentDateTime").isEqualTo("2050-10-17T11:00:00");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[1].assignmentReason").isEqualTo("ADM");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[1].agencyId").isEqualTo("LEI");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[1].description").isEqualTo("LEI-H-1-3");
        assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.content[1].bedAssignmentHistorySequence").isEqualTo(3);
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[1].movementMadeBy").isEqualTo("SA");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[1].offenderNo").isEqualTo("A1180MA");

        assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.content[2].bookingId").isEqualTo(-36);
        assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.content[2].livingUnitId").isEqualTo(-16);
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[2].assignmentDate").isEqualTo("2040-10-17");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[2].assignmentDateTime").isEqualTo("2040-10-17T11:00:00");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[2].assignmentReason").isEqualTo("ADM");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[2].agencyId").isEqualTo("LEI");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[2].description").isEqualTo("LEI-H-1-2");
        assertThat(bodyAsJsonContent).extractingJsonPathNumberValue("$.content[2].bedAssignmentHistorySequence").isEqualTo(2);
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[2].movementMadeBy").isEqualTo("SA");
        assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.content[2].offenderNo").isEqualTo("A1180MA");
    }

    @Nested
    public class GetProvenAdjudications {

        @Test
        public void returns403IfInvalidRole() {
            final var token = validToken(List.of("ROLE_DUMMY"));
            final var httpEntity = createHttpEntity(token, List.of(-5, -200));

            final var response = testRestTemplate.exchange(
                "/api/bookings/proven-adjudications",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 403);
        }

        @Test
        public void returnsDataForValidRole() {
            final var token = validToken(List.of("ROLE_VIEW_ADJUDICATIONS"));
            final var httpEntity = createHttpEntity(token, List.of(-5, -200));

            final var response = testRestTemplate.exchange(
                "/api/bookings/proven-adjudications",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 200);
        }

        @Test
        public void returnsValidData() {
            final var token = validToken(List.of("ROLE_VIEW_ADJUDICATIONS"));
            final var httpEntity = createHttpEntity(token, List.of(-5,-8));

            final var response = testRestTemplate.exchange(
                "/api/bookings/proven-adjudications?adjudicationCutoffDate=2017-09-13",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 200, "proven_adjudications.json");
        }
    }

    @Nested
    public class getBookingVisitsPrisons {
        @Test
        public void success() {
            final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits/prisons", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, -1L);

            final var bodyAsJsonContent = getBodyAsJsonContent(response);
            assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[0].prisonId").isEqualTo("LEI");
            assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[0].prison").isEqualTo("Leeds");
            assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[1].prisonId").isEqualTo("MDI");
            assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[1].prison").isEqualTo("Moorland");
            assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[2].prisonId").isEqualTo("BXI");
            assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$[2].prison").isEqualTo("Brixton");
        }

        @Test
        public void forbidden() {
            final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits/prisons", GET,
                createHttpEntity(createJwt("NO_USER", Collections.emptyList()), null),
                String.class, -1L);

            assertThatStatus(response, 404);
        }
    }

    @Nested
    public class getBookingVisitsSummary {
        @Test
        public void success_visits() {
            final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits/summary", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, -3L);

            final var bodyAsJsonContent = getBodyAsJsonContent(response);
            assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.startDateTime").isEqualTo(
                LocalDateTime.now()
                    .truncatedTo(ChronoUnit.DAYS)
                    .plus(1, ChronoUnit.DAYS)
                    .plus(10, ChronoUnit.HOURS)
                    .format(DateTimeFormatter.ISO_DATE_TIME));
            assertThat(bodyAsJsonContent).extractingJsonPathBooleanValue("$.hasVisits").isEqualTo(true);
        }
        @Test
        public void success_nonextvisit() {
            final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits/summary", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, -1L);

            final var bodyAsJsonContent = getBodyAsJsonContent(response);
            assertThat(bodyAsJsonContent).extractingJsonPathBooleanValue("$.hasVisits").isEqualTo(true);
            assertThat(bodyAsJsonContent).extractingJsonPathStringValue("$.startDateTime").isBlank();
        }

        @Test
        public void forbidden() {
            final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/visits/summary", GET,
                createHttpEntity(createJwt("NO_USER", Collections.emptyList()), null),
                String.class, -1L);

            assertThatStatus(response, 404);
        }
    }
}
