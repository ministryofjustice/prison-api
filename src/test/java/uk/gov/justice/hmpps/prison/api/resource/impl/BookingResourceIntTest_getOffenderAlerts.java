package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
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
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 1, 'R', 'RSS', 'ACTIVE', 'N', '2021-09-01', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 2, 'V', 'VOP', 'ACTIVE', 'N', '2021-09-02', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 3, 'R', 'RSS', 'ACTIVE', 'N', '2021-09-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-06-19', -56, -1035, 4, 'R', 'RSS', 'ACTIVE', 'N', '2021-09-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-08-19', -56, -1035, 5, 'X', 'XTACT', 'ACTIVE', 'N', '2021-09-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 6, 'R', 'RSS', 'ACTIVE', 'N', '2021-08-01', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 7, 'R', 'RSS', 'ACTIVE', 'N', '2021-08-02', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 8, 'R', 'RSS', 'ACTIVE', 'N', '2021-08-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 9, 'R', 'RSS', 'INACTIVE', 'N', '2021-08-04', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 10, 'R', 'RSS', 'INACTIVE', 'N', '2021-10-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 11, 'X', 'XCU', 'INACTIVE', 'N', '2021-10-02', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 12, 'X', 'XCU', 'ACTIVE', 'N', '2021-10-01', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
    */

    @Nested
    @DisplayName("GET /api/bookings/{bookingId}/alert")
    class LegacyUnSafeEndpoint {
        @Test
        @DisplayName("will return a page of alerts sorted by DESC by dateExpires,dateCreated which is ALERT_DATE not CREATE_DATETIME with no filters or sorting")
        void willReturnAllActiveAlertsWhenNoFilter() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("$").hasSize(10);

            assertThat(jsonContent).extractingJsonPathStringValue("[0].dateExpires").isEqualTo("2021-10-03");
            assertThat(jsonContent).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("[1].dateExpires").isEqualTo("2021-10-02");
            assertThat(jsonContent).extractingJsonPathStringValue("[1].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("[2].dateExpires").isEqualTo("2021-10-01");
            assertThat(jsonContent).extractingJsonPathStringValue("[2].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("[3].dateExpires").isEqualTo("2021-09-03");
            assertThat(jsonContent).extractingJsonPathStringValue("[3].dateCreated").isEqualTo("2021-08-19");

            assertThat(jsonContent).extractingJsonPathStringValue("[4].dateExpires").isEqualTo("2021-09-03");
            assertThat(jsonContent).extractingJsonPathStringValue("[4].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("[5].dateExpires").isEqualTo("2021-09-03");
            assertThat(jsonContent).extractingJsonPathStringValue("[5].dateCreated").isEqualTo("2021-06-19");

            assertThat(jsonContent).extractingJsonPathStringValue("[6].dateExpires").isEqualTo("2021-09-02");
            assertThat(jsonContent).extractingJsonPathStringValue("[6].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("[7].dateExpires").isEqualTo("2021-09-01");
            assertThat(jsonContent).extractingJsonPathStringValue("[7].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("[8].dateExpires").isEqualTo("2021-08-04");
            assertThat(jsonContent).extractingJsonPathStringValue("[8].dateCreated").isEqualTo("2021-07-19");

            assertThat(jsonContent).extractingJsonPathStringValue("[9].dateExpires").isEqualTo("2021-08-03");
            assertThat(jsonContent).extractingJsonPathStringValue("[9].dateCreated").isEqualTo("2021-07-19");
        }
        @Test
        @DisplayName("can control sort order")
        void canControlSortOrder() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of("Sort-Order", "DESC", "Sort-Fields", "dateCreated")),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("$").hasSize(10);

            assertThat(jsonContent).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-08-19");
            assertThat(jsonContent).extractingJsonPathStringValue("[1].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("[2].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("[3].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("[4].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("[5].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("[6].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("[7].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("[8].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("[9].dateCreated").isEqualTo("2021-07-19");
        }

        @Test
        @DisplayName("page totals are returned in the response headers")
        void pageTotalsAreReturnedInTheResponseHeaders() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56));

            assertThat(response.getHeaders().get("Total-Records")).containsExactly("12");
            assertThat(response.getHeaders().get("Page-Offset")).containsExactly("0");
            assertThat(response.getHeaders().get("Page-Limit")).containsExactly("10");
        }

        @Test
        @DisplayName("can request second page of results")
        void canRequestSecondPageOfResults() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of("Page-Offset", "10")),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("$").hasSize(2);
            assertThat(response.getHeaders().get("Total-Records")).containsExactly("12");
            assertThat(response.getHeaders().get("Page-Offset")).containsExactly("10");
            assertThat(response.getHeaders().get("Page-Limit")).containsExactly("10");
        }

        @Test
        @DisplayName("can filter by alert type")
        void canFilterByAlertType() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "query", "alertType:in:'X'"));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("$").hasSize(3);

            assertThat(jsonContent).extractingJsonPathStringValue("[0].alertType").isEqualTo("X");
            assertThat(jsonContent).extractingJsonPathStringValue("[0].alertCode").isEqualTo("XCU");
            assertThat(jsonContent).extractingJsonPathStringValue("[1].alertType").isEqualTo("X");
            assertThat(jsonContent).extractingJsonPathStringValue("[1].alertCode").isEqualTo("XCU");
            assertThat(jsonContent).extractingJsonPathStringValue("[2].alertType").isEqualTo("X");
            assertThat(jsonContent).extractingJsonPathStringValue("[2].alertCode").isEqualTo("XTACT");
        }

        @Test
        @DisplayName("can filter by active state")
        void canFilterByActiveState() {
            final var activeResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "query", "active:eq:'ACTIVE'"));

            assertThat(getBodyAsJsonContent(activeResponse)).extractingJsonPathArrayValue("$").hasSize(9);


            final var inactiveResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "query", "active:eq:'INACTIVE'"));

            assertThat(getBodyAsJsonContent(inactiveResponse)).extractingJsonPathArrayValue("$").hasSize(3);
        }
        @Test
        @DisplayName("can filter by before createDate (alert date)")
        void canFilterByBeforeCreateDate() {
            final var oldestAlertResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "query", "dateCreated:lteq:DATE'2021-06-19'"));

            assertThat(getBodyAsJsonContent(oldestAlertResponse)).extractingJsonPathArrayValue("$").hasSize(1);
            assertThat(getBodyAsJsonContent(oldestAlertResponse)).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-06-19");

            final var justBeforeSecondOldestAlertResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "query", "dateCreated:lteq:DATE'2021-07-18'"));

            assertThat(getBodyAsJsonContent(justBeforeSecondOldestAlertResponse)).extractingJsonPathArrayValue("$").hasSize(1);
            assertThat(getBodyAsJsonContent(justBeforeSecondOldestAlertResponse)).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-06-19");
        }
        @Test
        @DisplayName("can filter by after createDate (alert date)")
        void canFilterByAfterCreateDate() {
            final var youngestAlertResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "query", "dateCreated:gteq:DATE'2021-08-19'"));

            assertThat(getBodyAsJsonContent(youngestAlertResponse)).extractingJsonPathArrayValue("$").hasSize(1);
            assertThat(getBodyAsJsonContent(youngestAlertResponse)).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-08-19");

            final var justAfterSecondYoungestAlertResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "query", "dateCreated:gteq:DATE'2021-07-20'"));

            assertThat(getBodyAsJsonContent(justAfterSecondYoungestAlertResponse)).extractingJsonPathArrayValue("$").hasSize(1);
            assertThat(getBodyAsJsonContent(justAfterSecondYoungestAlertResponse)).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-08-19");
        }

        @Test
        @DisplayName("can filter between inclusive two dates")
        void canFilterBetweenInclusiveTwoDates() {
            final var middleDatesResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "query", "dateCreated:lteq:DATE'2021-07-19',and:dateCreated:gteq:DATE'2021-07-19'"));

            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathArrayValue("$").hasSize(10);
            assertThat(middleDatesResponse.getHeaders().get("Total-Records")).containsExactly("10");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("[1].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("[2].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("[3].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("[4].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("[5].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("[6].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("[7].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("[8].dateCreated").isEqualTo("2021-07-19");
            assertThat(getBodyAsJsonContent(middleDatesResponse)).extractingJsonPathStringValue("[9].dateCreated").isEqualTo("2021-07-19");

            final var justBeforeAndAfterMiddleDatesResponse = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "query", "dateCreated:gteq:DATE'2021-07-18',and:dateCreated:lteq:DATE'2021-07-20'"));

            assertThat(getBodyAsJsonContent(justBeforeAndAfterMiddleDatesResponse)).extractingJsonPathArrayValue("$").hasSize(10);
            assertThat(justBeforeAndAfterMiddleDatesResponse.getHeaders().get("Total-Records")).containsExactly("10");
        }

        @Test
        @DisplayName("can filter be dateCreated and active state and alert type")
        void canFilterBeDateCreatedAndActiveStateAndAlertType() {
            final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA, Map.of()),
                new ParameterizedTypeReference<String>() {
                },
                Map.of("bookingId", -56, "query", "dateCreated:lteq:DATE'2021-07-19',and:dateCreated:gteq:DATE'2021-07-19',and:active:eq:'INACTIVE',and:alertType:in:'X'"));

            final var jsonContent = getBodyAsJsonContent(response);

            assertThat(jsonContent).extractingJsonPathArrayValue("$").hasSize(1);
            assertThat(jsonContent).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-07-19");
            assertThat(jsonContent).extractingJsonPathStringValue("[0].alertType").isEqualTo("X");
            assertThat(jsonContent).extractingJsonPathBooleanValue("[0].active").isFalse();
        }
    }
}
