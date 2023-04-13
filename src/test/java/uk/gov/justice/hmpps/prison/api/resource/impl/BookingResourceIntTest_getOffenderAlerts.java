package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

@DisplayName("BookingResource get offender alerts")
public class BookingResourceIntTest_getOffenderAlerts extends ResourceTest {
    /*
    data is as follows (see resources/db/migration/data/R__4_3__OFFENDER_ALERTS.sql)
    offender A1179MT
    has 2 bookings; booking seq 1 -35  and booking seq 2 -56
    relevant data is as follows:
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, MODIFY_USER_ID, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 1, 'R', 'RSS', 'ACTIVE', 'N', '2121-09-01', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER','ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 2, 'V', 'VOP', 'ACTIVE', 'N', '2121-09-02', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 3, 'R', 'RSS', 'ACTIVE', 'N', '2121-09-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-06-19', -56, -1035, 4, 'R', 'RSS', 'ACTIVE', 'N', '2121-09-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-08-19', -56, -1035, 5, 'X', 'XTACT', 'ACTIVE', 'N', '2121-09-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 6, 'R', 'RSS', 'ACTIVE', 'N', '2021-08-01', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 7, 'R', 'RSS', 'ACTIVE', 'N', '2021-08-02', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 8, 'R', 'RSS', 'ACTIVE', 'N', '2021-08-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 9, 'R', 'RSS', 'INACTIVE', 'N', '2021-08-04', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 10, 'R', 'RSS', 'INACTIVE', 'N', '2121-10-03', 'Expired risk alert', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 11, 'X', 'XCU', 'INACTIVE', 'N', '2121-10-02', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 12, 'X', 'XCU', 'ACTIVE', 'N', '2121-10-01', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
    */

    @Nested
    @DisplayName("GET /api/bookings/{bookingId}/alert/v2")
    class NewSafeEndpoint {

        @Test
        @DisplayName("should have the correct role to access booking")
        void shouldHaveTheCorrectRoleToAccessEndpoint() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2",
                GET,
                createEmptyHttpEntity(AuthToken.RENEGADE_USER),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("returns partial alert data for each alert")
        void returnsImportantAlertDataForEachAlert() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("content").hasSize(10);

            // not returned when looking up by bookingId
            assertThat(jsonContent).extractingJsonPathNumberValue("content[0].bookingId").isNull();
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].offenderNo").isNull();

            assertThat(jsonContent).extractingJsonPathNumberValue("content[0].alertId").isEqualTo(10);
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertType").isEqualTo("R");
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertTypeDescription").isEqualTo("Risk");
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertCode").isEqualTo("RSS");
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertCodeDescription").isEqualTo("Risk to Staff - Custody");
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].comment").isEqualTo("Expired risk alert");
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateExpires").isEqualTo("2121-10-03");
            assertThat(jsonContent).extractingJsonPathBooleanValue("content[0].expired").isEqualTo(false);
            assertThat(jsonContent).extractingJsonPathBooleanValue("content[0].active").isEqualTo(false);
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].addedByFirstName").isEqualTo("API");
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].addedByLastName").isEqualTo("USER");
            // not returned when nobody has ever updated this alert
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].expiredByFirstName").isNull();
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].expiredByLastName").isNull();
        }

        @Test
        @DisplayName("expiredBy user is actually the last updated user and nothing to do with expiry")
        void expiredByUserIsActuallyTheLastUpdatedUserAndNothingToDoWithExpiry() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("content").hasSize(10);

            assertThat(jsonContent).extractingJsonPathNumberValue("content[7].alertId").isEqualTo(1);
            assertThat(jsonContent).extractingJsonPathBooleanValue("content[7].expired").isEqualTo(false);
            assertThat(jsonContent).extractingJsonPathBooleanValue("content[7].active").isEqualTo(true);
            assertThat(jsonContent).extractingJsonPathStringValue("content[7].expiredByFirstName").isEqualTo("API");
            assertThat(jsonContent).extractingJsonPathStringValue("content[7].expiredByLastName").isEqualTo("USER");

            // not returned when nobody has ever updated this alert
            assertThat(jsonContent).extractingJsonPathStringValue("[0].expiredByFirstName").isNull();
            assertThat(jsonContent).extractingJsonPathStringValue("[0].expiredByLastName").isNull();
        }


        @Test
        @DisplayName("will return a page of alerts sorted by DESC by dateExpires,dateCreated which is ALERT_DATE not CREATE_DATETIME with no filters or sorting")
        void willReturnAllActiveAlertsWhenNoFilter() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("content").hasSize(10);

            assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateExpires").isEqualTo("2121-10-03");
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("content[1].dateExpires").isEqualTo("2121-10-02");
            assertThat(jsonContent).extractingJsonPathStringValue("content[1].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("content[2].dateExpires").isEqualTo("2121-10-01");
            assertThat(jsonContent).extractingJsonPathStringValue("content[2].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("content[3].dateExpires").isEqualTo("2121-09-03");
            assertThat(jsonContent).extractingJsonPathStringValue("content[3].dateCreated").isEqualTo("2021-08-19");

            assertThat(jsonContent).extractingJsonPathStringValue("content[4].dateExpires").isEqualTo("2121-09-03");
            assertThat(jsonContent).extractingJsonPathStringValue("content[4].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("content[5].dateExpires").isEqualTo("2121-09-03");
            assertThat(jsonContent).extractingJsonPathStringValue("content[5].dateCreated").isEqualTo("2021-06-19");

            assertThat(jsonContent).extractingJsonPathStringValue("content[6].dateExpires").isEqualTo("2121-09-02");
            assertThat(jsonContent).extractingJsonPathStringValue("content[6].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("content[7].dateExpires").isEqualTo("2121-09-01");
            assertThat(jsonContent).extractingJsonPathStringValue("content[7].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("content[8].dateExpires").isEqualTo("2021-08-04");
            assertThat(jsonContent).extractingJsonPathStringValue("content[8].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("content[9].dateExpires").isEqualTo("2021-08-03");
            assertThat(jsonContent).extractingJsonPathStringValue("content[9].dateCreated").isEqualTo("2021-07-19");
        }
        @Test
        @DisplayName("can control sort order")
        void canControlSortOrder() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?sort={sort}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "sort", "dateCreated,desc"));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("content").hasSize(10);

            assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-08-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[1].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[2].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[3].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[4].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[5].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[6].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[7].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[8].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[9].dateCreated").isEqualTo("2021-07-19");
        }

        @Test
        @DisplayName("page totals are returned in the response body")
        void pageTotalsAreReturnedInTheResponseHeaders() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathNumberValue("totalElements").isEqualTo(12);
            assertThat(jsonContent).extractingJsonPathNumberValue("numberOfElements").isEqualTo(10);
            assertThat(jsonContent).extractingJsonPathNumberValue("number").isEqualTo(0);
            assertThat(jsonContent).extractingJsonPathNumberValue("totalPages").isEqualTo(2);
            assertThat(jsonContent).extractingJsonPathNumberValue("size").isEqualTo(10);
        }

        @Test
        @DisplayName("can request second page of results")
        void canRequestSecondPageOfResults() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?page={page}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "page", 1));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("content").hasSize(2);
            assertThat(jsonContent).extractingJsonPathNumberValue("numberOfElements").isEqualTo(2);
            assertThat(jsonContent).extractingJsonPathNumberValue("totalElements").isEqualTo(12);
            assertThat(jsonContent).extractingJsonPathNumberValue("number").isEqualTo(1);
            assertThat(jsonContent).extractingJsonPathNumberValue("totalPages").isEqualTo(2);
            assertThat(jsonContent).extractingJsonPathNumberValue("size").isEqualTo(10);
        }

        @Test
        @DisplayName("can filter by alert type")
        void canFilterByAlertType() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?alertType={alertType}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "alertType", "X"));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("content").hasSize(3);

            assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertType").isEqualTo("X");
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertCode").isEqualTo("XCU");
            assertThat(jsonContent).extractingJsonPathStringValue("content[1].alertType").isEqualTo("X");
            assertThat(jsonContent).extractingJsonPathStringValue("content[1].alertCode").isEqualTo("XCU");
            assertThat(jsonContent).extractingJsonPathStringValue("content[2].alertType").isEqualTo("X");
            assertThat(jsonContent).extractingJsonPathStringValue("content[2].alertCode").isEqualTo("XTACT");
        }

        @Test
        @DisplayName("can filter by active state")
        void canFilterByActiveState() {
            final var activeResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?alertStatus={alertStatus}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "alertStatus", "ACTIVE"));

            assertThat(getBodyAsJsonContent(activeResponse)).extractingJsonPathArrayValue("content").hasSize(9);


            final var inactiveResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?alertStatus={alertStatus}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "alertStatus", "INACTIVE"));

            assertThat(getBodyAsJsonContent(inactiveResponse)).extractingJsonPathArrayValue("content").hasSize(3);
        }
        @Test
        @DisplayName("can filter by before createDate (alert date)")
        void canFilterByBeforeCreateDate() {
            final var oldestAlertResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?to={to}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "to", "2021-06-19"));

            assertThat(getBodyAsJsonContent(oldestAlertResponse)).extractingJsonPathArrayValue("content").hasSize(1);
            assertThat(getBodyAsJsonContent(oldestAlertResponse)).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-06-19");

            final var justBeforeSecondOldestAlertResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?to={to}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "to", "2021-07-18"));

            assertThat(getBodyAsJsonContent(justBeforeSecondOldestAlertResponse)).extractingJsonPathArrayValue("content").hasSize(1);
            assertThat(getBodyAsJsonContent(justBeforeSecondOldestAlertResponse)).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-06-19");
        }
        @Test
        @DisplayName("can filter by after createDate (alert date)")
        void canFilterByAfterCreateDate() {
            final var youngestAlertResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?from={from}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "from", "2021-08-19"));

            assertThat(getBodyAsJsonContent(youngestAlertResponse)).extractingJsonPathArrayValue("content").hasSize(1);
            assertThat(getBodyAsJsonContent(youngestAlertResponse)).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-08-19");

            final var justAfterSecondYoungestAlertResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?from={from}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "from", "2021-07-20"));

            assertThat(getBodyAsJsonContent(justAfterSecondYoungestAlertResponse)).extractingJsonPathArrayValue("content").hasSize(1);
            assertThat(getBodyAsJsonContent(justAfterSecondYoungestAlertResponse)).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-08-19");
        }

        @Test
        @DisplayName("can filter between inclusive two dates")
        void canFilterBetweenInclusiveTwoDates() {
            final var middleDatesResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?from={from}&to={to}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "from", "2021-07-19", "to", "2021-07-19"));

            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathArrayValue("content").hasSize(10);
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathNumberValue("totalElements").isEqualTo(10);
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("content[1].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("content[2].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("content[3].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("content[4].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("content[5].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("content[6].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("content[7].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("content[8].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("content[9].dateCreated").isEqualTo("2021-07-19");

            final var justBeforeAndAfterMiddleDatesResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?from={from}&to={to}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "from", "2021-07-18", "to", "2021-07-20"));

            assertThat(getBodyAsJsonContent(justBeforeAndAfterMiddleDatesResponse)).extractingJsonPathArrayValue("content").hasSize(10);
            assertThat(getBodyAsJsonContent(justBeforeAndAfterMiddleDatesResponse)).extractingJsonPathNumberValue("totalElements").isEqualTo(10);
        }

        @Test
        @DisplayName("can filter be dateCreated and active state and alert type")
        void canFilterBeDateCreatedAndActiveStateAndAlertType() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?alertType={alertType}&from={from}&to={to}&alertStatus={alertStatus}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "from", "2021-07-19", "to", "2021-07-19", "alertType", "X", "alertStatus", "INACTIVE"));

            final var jsonContent = getBodyAsJsonContent(response);

            assertThat(jsonContent).extractingJsonPathArrayValue("content").hasSize(1);
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertType").isEqualTo("X");
            assertThat(jsonContent).extractingJsonPathBooleanValue("content[0].active").isFalse();
        }

        @Test
        @DisplayName("empty filter parameters are ignored")
        void emptyFilterParametersAreIgnored() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts/v2?alertType={alertType}&from={from}&to={to}&alertStatus={alertStatus}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "from", "", "to", "", "alertType", "", "alertStatus", ""));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("content").hasSize(10);
            assertThat(jsonContent).extractingJsonPathNumberValue("totalElements").isEqualTo(12);
        }
    }
}
