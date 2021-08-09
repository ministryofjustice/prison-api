package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

@DisplayName("OffenderResource get offender alerts")
public class OffenderResourceIntTest_getOffenderAlerts extends ResourceTest {
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

        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME) VALUES (DATE '2021-07-20', -35, -1035, 1, 'V', 'VOP', 'ACTIVE', 'N', null, 'whatever', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME) VALUES (DATE '2021-07-21', -35, -1035, 2, 'X', 'XTACT', 'INACTIVE', 'N', sysdate, '', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME) VALUES (DATE '2021-07-22', -35, -1035, 3, 'X', 'XCU', 'ACTIVE', 'N', null, '', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0');
    */

    @Nested
    @DisplayName("GET /api/offenders/{offenderNo}/alerts")
    class LegacyUnSafeEndpoint {
        @Test
        @DisplayName("will return all alerts for latest booking only when no filter")
        void willReturnAllActiveAlertsWhenNoFilter() {
            final var response = testRestTemplate.exchange(
                "/api/offenders/{offenderNo}/alerts",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
                new ParameterizedTypeReference<String>() {
                },
                "A1179MT");

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("$").hasSize(3);

            assertThat(jsonContent).extractingJsonPathBooleanValue("[0].active").isEqualTo(true);
            assertThat(jsonContent).extractingJsonPathStringValue("[0].alertCode").isEqualTo("VOP");
            assertThat(jsonContent)
                .extractingJsonPathStringValue("[0].alertCodeDescription")
                .isEqualTo("Rule 45 - Own Protection");
            assertThat(jsonContent).extractingJsonPathNumberValue("[0].alertId").isEqualTo(1);
            assertThat(jsonContent).extractingJsonPathStringValue("[0].alertType").isEqualTo("V");
            assertThat(jsonContent)
                .extractingJsonPathStringValue("[0].alertTypeDescription")
                .isEqualTo("Vulnerability");
            assertThat(jsonContent).extractingJsonPathNumberValue("[0].bookingId").isEqualTo(-35);
            assertThat(jsonContent).extractingJsonPathStringValue("[0].comment").isEqualTo("whatever");
            assertThat(jsonContent).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-07-20");
            assertThat(jsonContent).extractingJsonPathBooleanValue("[0].expired").isEqualTo(false);
            assertThat(jsonContent).extractingJsonPathStringValue("[0].offenderNo").isEqualTo("A1179MT");

            assertThat(jsonContent).extractingJsonPathStringValue("[1].alertType").isEqualTo("X");
            assertThat(jsonContent).extractingJsonPathStringValue("[1].alertCode").isEqualTo("XTACT");
            assertThat(jsonContent).extractingJsonPathNumberValue("[1].bookingId").isEqualTo(-35);
            assertThat(jsonContent).extractingJsonPathBooleanValue("[1].active").isEqualTo(false);
            assertThat(jsonContent).extractingJsonPathBooleanValue("[1].expired").isEqualTo(true);

            assertThat(jsonContent).extractingJsonPathNumberValue("[2].bookingId").isEqualTo(-35);
        }

        @Test
        @DisplayName("will shows alerts for all of an offenders bookings when latest only set to false ordered by bookingId ascending")
        void willShowsAlertsForAllOfAnOffendersBookingsWhenFilterSupplied() {
            final var response = testRestTemplate.exchange(
                "/api/offenders/{offenderNo}/alerts?latestOnly={latestOnly}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
                new ParameterizedTypeReference<String>() {
                },
                Map.of(
                    "offenderNo", "A1179MT",
                    "latestOnly", false));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("$").hasSize(15);
            assertThat(jsonContent).extractingJsonPathNumberValue("[0].bookingId").isEqualTo(-56);
            assertThat(jsonContent).extractingJsonPathNumberValue("[11].bookingId").isEqualTo(-56);
            assertThat(jsonContent).extractingJsonPathNumberValue("[12].bookingId").isEqualTo(-35);
            assertThat(jsonContent).extractingJsonPathNumberValue("[13].bookingId").isEqualTo(-35);
            assertThat(jsonContent).extractingJsonPathNumberValue("[14].bookingId").isEqualTo(-35);

        }

        @Test
        @DisplayName("will show alerts for the latest booking of an offender when latest only set to true")
        void willShowAlertsForTheLatestBookingOfAnOffenderWhenLatestOnlySetToTrue() {
            final var response = testRestTemplate.exchange(
                "/api/offenders/{offenderNo}/alerts?latestOnly={latestOnly}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
                new ParameterizedTypeReference<String>() {
                },
                Map.of(
                    "offenderNo", "A1179MT",
                    "latestOnly", true));

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("$").hasSize(3);
            assertThat(jsonContent).extractingJsonPathNumberValue("[0].bookingId").isEqualTo(-35);
            assertThat(jsonContent).extractingJsonPathNumberValue("[1].bookingId").isEqualTo(-35);
            assertThat(jsonContent).extractingJsonPathNumberValue("[2].bookingId").isEqualTo(-35);
        }

        @Test
        @DisplayName("can filter by alert code")
        void canFilterByAlertCode() {
            //noinspection Convert2Diamond
            var jsonContent = getBodyAsJsonContent(testRestTemplate.exchange(
                "/api/offenders/{offenderNo}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
                new ParameterizedTypeReference<String>() {
                },
                Map.of(
                    "offenderNo", "A1179MT",
                    "query", "alertCode:eq:'XTACT',or:alertCode:eq:'XCU'")));
            assertThat(jsonContent).extractingJsonPathArrayValue("$").hasSize(2);
            assertThat(jsonContent).extractingJsonPathStringValue("[0].alertCode").isEqualTo("XTACT");
            assertThat(jsonContent).extractingJsonPathStringValue("[1].alertCode").isEqualTo("XCU");

            //noinspection Convert2Diamond
            jsonContent = getBodyAsJsonContent(testRestTemplate.exchange(
                "/api/offenders/{offenderNo}/alerts?query={query}",
                GET,
                createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
                new ParameterizedTypeReference<String>() {
                },
                Map.of(
                    "offenderNo", "A1179MT",
                    "query", "alertCode:eq:'XTACT'")));
            assertThat(jsonContent).extractingJsonPathArrayValue("$").hasSize(1);
            assertThat(jsonContent).extractingJsonPathStringValue("[0].alertCode").isEqualTo("XTACT");
        }
    }
}
