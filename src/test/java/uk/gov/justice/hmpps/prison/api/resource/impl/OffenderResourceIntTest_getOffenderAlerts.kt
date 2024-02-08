@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken

@DisplayName("OffenderResource get offender alerts")
class OffenderResourceIntTest_getOffenderAlerts : ResourceTest() {
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

        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME) VALUES (DATE '2021-07-20', -35, -1035, 1, 'X', 'XTACT', 'ACTIVE', 'N', null, 'whatever', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME) VALUES (DATE '2021-07-21', -35, -1035, 2, 'V', 'VOP', 'INACTIVE', 'N', '2021-08-01', '', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME) VALUES (DATE '2021-07-22', -35, -1035, 3, 'X', 'XCU', 'ACTIVE', 'N', null, '', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0');
     */
  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/bookings/latest/alerts")
  internal inner class NewSafeEndpoint {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/offenders/A1179MT/alerts/v2")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/offenders/A1179MT/bookings/latest/alerts")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when client has override role ROLE_SYSTEM_USER `() {
      webTestClient.get().uri("/api/offenders/A1179MT/bookings/latest/alerts")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    @DisplayName("should have the correct role to access offender")
    fun shouldHaveTheCorrectRoleToAccessEndpoint() {
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}/bookings/latest/alerts",
        GET,
        createEmptyHttpEntity(AuthToken.RENEGADE_USER),
        object : ParameterizedTypeReference<String?>() {
        },
        "A1179MT",
      )

      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @DisplayName("returns partial alert data for each alert")
    fun returnsImportantAlertDataForEachAlert() {
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}/bookings/latest/alerts",
        GET,
        createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
        object : ParameterizedTypeReference<String?>() {
        },
        "A1179MT",
      )

      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(3)

      assertThat(jsonContent).extractingJsonPathNumberValue("[0].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].offenderNo").isEqualTo("A1179MT")
      assertThat(jsonContent).extractingJsonPathNumberValue("[0].alertId").isEqualTo(2)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].alertCode").isEqualTo("VOP")
      assertThat(jsonContent)
        .extractingJsonPathStringValue("[0].alertCodeDescription")
        .isEqualTo("Rule 45 - Own Protection")
      assertThat(jsonContent).extractingJsonPathStringValue("[0].alertType").isEqualTo("V")
      assertThat(jsonContent)
        .extractingJsonPathStringValue("[0].alertTypeDescription")
        .isEqualTo("Vulnerability")
      assertThat(jsonContent).extractingJsonPathStringValue("[0].comment").isEqualTo("")
      assertThat(jsonContent).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-07-21")
      assertThat(jsonContent).extractingJsonPathStringValue("[0].dateExpires").isEqualTo("2021-08-01")
      assertThat(jsonContent).extractingJsonPathBooleanValue("[0].expired").isEqualTo(true)
      assertThat(jsonContent).extractingJsonPathBooleanValue("[0].active").isEqualTo(false)

      // not returned when retrieving by offender number
      assertThat(jsonContent).extractingJsonPathStringValue("[0].addedByFirstName").isNull()
      assertThat(jsonContent).extractingJsonPathStringValue("[0].addedByLastName").isNull()
      assertThat(jsonContent).extractingJsonPathStringValue("[0].expiredByFirstName").isNull()
      assertThat(jsonContent).extractingJsonPathStringValue("[0].expiredByLastName").isNull()
    }

    @Test
    @DisplayName("will return all alerts for latest booking only when no filter")
    fun willReturnAllActiveAlertsWhenNoFilter() {
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}/bookings/latest/alerts",
        GET,
        createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
        object : ParameterizedTypeReference<String?>() {
        },
        "A1179MT",
      )

      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(3)

      assertThat(jsonContent).extractingJsonPathBooleanValue("[0].active").isEqualTo(false)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].alertCode").isEqualTo("VOP")
      assertThat(jsonContent)
        .extractingJsonPathStringValue("[0].alertCodeDescription")
        .isEqualTo("Rule 45 - Own Protection")
      assertThat(jsonContent).extractingJsonPathStringValue("[0].alertType").isEqualTo("V")
      assertThat(jsonContent)
        .extractingJsonPathStringValue("[0].alertTypeDescription")
        .isEqualTo("Vulnerability")
      assertThat(jsonContent).extractingJsonPathNumberValue("[0].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].dateCreated").isEqualTo("2021-07-21")
      assertThat(jsonContent).extractingJsonPathBooleanValue("[0].expired").isEqualTo(true)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].offenderNo").isEqualTo("A1179MT")

      assertThat(jsonContent).extractingJsonPathStringValue("[1].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathStringValue("[1].alertCode").isEqualTo("XTACT")
      assertThat(jsonContent).extractingJsonPathStringValue("[1].alertCodeDescription").isEqualTo("XTACT")
      assertThat(jsonContent).extractingJsonPathNumberValue("[1].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathBooleanValue("[1].active").isEqualTo(true)
      assertThat(jsonContent).extractingJsonPathBooleanValue("[1].expired").isEqualTo(false)
      assertThat(jsonContent).extractingJsonPathStringValue("[1].comment").isEqualTo("whatever")

      assertThat(jsonContent).extractingJsonPathNumberValue("[2].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathStringValue("[2].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathStringValue("[2].alertCode").isEqualTo("XCU")
    }

    @Test
    @DisplayName("will order by alert type of an offender")
    fun willShowAlertsForTheLatestBookingOfAnOffenderWhenLatestOnlySetToTrue() {
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}/bookings/latest/alerts",
        GET,
        createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
        object : ParameterizedTypeReference<String?>() {
        },
        mapOf(
          "offenderNo" to "A1179MT",
        ),
      )

      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(3)
      assertThat(jsonContent).extractingJsonPathNumberValue("[0].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].alertType").isEqualTo("V")
      assertThat(jsonContent).extractingJsonPathNumberValue("[1].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathStringValue("[1].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathNumberValue("[2].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathStringValue("[2].alertType").isEqualTo("X")
    }

    @Test
    @DisplayName("can filter by alert code")
    fun canFilterByAlertCode() {
      var jsonContent = getBodyAsJsonContent<Any>(
        testRestTemplate.exchange(
          "/api/offenders/{offenderNo}/bookings/latest/alerts?alertCodes={alertCodes}",
          GET,
          createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
          object : ParameterizedTypeReference<String?>() {
          },
          mapOf(
            "offenderNo" to "A1179MT",
            "alertCodes" to "XTACT,XCU",
          ),
        ),
      )
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(2)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].alertCode").isEqualTo("XTACT")
      assertThat(jsonContent).extractingJsonPathStringValue("[1].alertCode").isEqualTo("XCU")

      jsonContent = getBodyAsJsonContent(
        testRestTemplate.exchange(
          "/api/offenders/{offenderNo}/bookings/latest/alerts?alertCodes={alertCodes}",
          GET,
          createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
          object : ParameterizedTypeReference<String?>() {
          },
          mapOf(
            "offenderNo" to "A1179MT",
            "alertCodes" to "XTACT",
          ),
        ),
      )
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(1)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].alertCode").isEqualTo("XTACT")
    }
  }

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/alerts/v2")
  internal inner class NewSafeEndpointAllBookings {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/offenders/A1179MT/alerts/v2")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/offenders/A1179MT/alerts/v2")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 404 if booking does not exist`() {
      webTestClient.get().uri("/api/offenders/A9999ZZ/alerts/v2")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [A9999ZZ] not found.")
    }

    @Test
    fun `returns 403 when client has override role ROLE_SYSTEM_USER `() {
      webTestClient.get().uri("/api/offenders/A1179MT/alerts/v2")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_GLOBAL_SEARCH `() {
      webTestClient.get().uri("/api/offenders/A1179MT/alerts/v2")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA `() {
      webTestClient.get().uri("/api/offenders/A1179MT/alerts/v2")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns success when client has override role ROLE_CREATE_CATEGORISATION `() {
      webTestClient.get().uri("/api/offenders/A1179MT/alerts/v2")
        .headers(setClientAuthorisation(listOf("ROLE_CREATE_CATEGORISATION")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns success when client has override role ROLE_APPROVE_CATEGORISATION `() {
      webTestClient.get().uri("/api/offenders/A1179MT/alerts/v2")
        .headers(setClientAuthorisation(listOf("ROLE_APPROVE_CATEGORISATION")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/offenders/A1179MT/alerts/v2")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `returns success if in user caseload`() {
      webTestClient.get().uri("/api/offenders/A1179MT/alerts/v2")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    @DisplayName("should have the correct role to access offender")
    fun shouldHaveTheCorrectRoleToAccessEndpoint() {
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}/alerts/v2",
        GET,
        createEmptyHttpEntity(AuthToken.RENEGADE_USER),
        object : ParameterizedTypeReference<String?>() {
        },
        "A1179MT",
      )

      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @DisplayName("returns partial alert data for each alert for each booking")
    fun returnsImportantAlertDataForEachAlertForEachBooking() {
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}/alerts/v2?sort=alertType,alertId,dateExpires",
        GET,
        createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
        object : ParameterizedTypeReference<String?>() {
        },
        "A1179MT",
      )

      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(15)

      assertThat(jsonContent).extractingJsonPathNumberValue("[8].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathStringValue("[8].offenderNo").isEqualTo("A1179MT")
      assertThat(jsonContent).extractingJsonPathNumberValue("[8].alertId").isEqualTo(2)
      assertThat(jsonContent).extractingJsonPathStringValue("[8].alertCode").isEqualTo("VOP")
      assertThat(jsonContent)
        .extractingJsonPathStringValue("[8].alertCodeDescription")
        .isEqualTo("Rule 45 - Own Protection")
      assertThat(jsonContent).extractingJsonPathStringValue("[8].alertType").isEqualTo("V")
      assertThat(jsonContent)
        .extractingJsonPathStringValue("[8].alertTypeDescription")
        .isEqualTo("Vulnerability")
      assertThat(jsonContent).extractingJsonPathStringValue("[8].comment").isEqualTo("")
      assertThat(jsonContent).extractingJsonPathStringValue("[8].dateCreated").isEqualTo("2021-07-21")
      assertThat(jsonContent).extractingJsonPathStringValue("[8].dateExpires").isEqualTo("2021-08-01")
      assertThat(jsonContent).extractingJsonPathBooleanValue("[8].expired").isEqualTo(true)
      assertThat(jsonContent).extractingJsonPathBooleanValue("[8].active").isEqualTo(false)

      // not returned when retrieving by offender number
      assertThat(jsonContent).extractingJsonPathStringValue("[8].addedByFirstName").isNull()
      assertThat(jsonContent).extractingJsonPathStringValue("[8].addedByLastName").isNull()
      assertThat(jsonContent).extractingJsonPathStringValue("[8].expiredByFirstName").isNull()
      assertThat(jsonContent).extractingJsonPathStringValue("[8].expiredByLastName").isNull()
    }

    @Test
    @DisplayName("will return all alerts for all booking only when no filter")
    fun willReturnAllActiveAlertsWhenNoFilter() {
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}/alerts/v2?sort=alertType,alertId,dateExpires",
        GET,
        createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
        object : ParameterizedTypeReference<String?>() {
        },
        "A1179MT",
      )

      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(15)

      assertThat(jsonContent).extractingJsonPathNumberValue("[0].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[0].alertId").isEqualTo(1)
      assertThat(jsonContent).extractingJsonPathNumberValue("[1].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[1].alertId").isEqualTo(3)
      assertThat(jsonContent).extractingJsonPathNumberValue("[2].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[2].alertId").isEqualTo(4)
      assertThat(jsonContent).extractingJsonPathNumberValue("[3].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[3].alertId").isEqualTo(6)
      assertThat(jsonContent).extractingJsonPathNumberValue("[4].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[4].alertId").isEqualTo(7)
      assertThat(jsonContent).extractingJsonPathNumberValue("[5].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[5].alertId").isEqualTo(8)
      assertThat(jsonContent).extractingJsonPathNumberValue("[6].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[6].alertId").isEqualTo(9)
      assertThat(jsonContent).extractingJsonPathNumberValue("[7].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[7].alertId").isEqualTo(10)
      assertThat(jsonContent).extractingJsonPathNumberValue("[8].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathNumberValue("[8].alertId").isEqualTo(2)
      assertThat(jsonContent).extractingJsonPathNumberValue("[9].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[9].alertId").isEqualTo(2)
      assertThat(jsonContent).extractingJsonPathNumberValue("[10].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathNumberValue("[10].alertId").isEqualTo(1)
      assertThat(jsonContent).extractingJsonPathNumberValue("[11].bookingId").isEqualTo(-35)
      assertThat(jsonContent).extractingJsonPathNumberValue("[11].alertId").isEqualTo(3)
      assertThat(jsonContent).extractingJsonPathNumberValue("[12].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[12].alertId").isEqualTo(5)
      assertThat(jsonContent).extractingJsonPathNumberValue("[13].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[13].alertId").isEqualTo(11)
      assertThat(jsonContent).extractingJsonPathNumberValue("[14].bookingId").isEqualTo(-56)
      assertThat(jsonContent).extractingJsonPathNumberValue("[14].alertId").isEqualTo(12)
    }

    @Test
    @DisplayName("will order by alert type of an offender")
    fun willOrderByAlertType() {
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}/alerts/v2",
        GET,
        createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
        object : ParameterizedTypeReference<String?>() {
        },
        mapOf(
          "offenderNo" to "A1179MT",
        ),
      )

      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(15)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].alertType").isEqualTo("R")
      assertThat(jsonContent).extractingJsonPathStringValue("[1].alertType").isEqualTo("R")
      assertThat(jsonContent).extractingJsonPathStringValue("[2].alertType").isEqualTo("R")
      assertThat(jsonContent).extractingJsonPathStringValue("[3].alertType").isEqualTo("R")
      assertThat(jsonContent).extractingJsonPathStringValue("[4].alertType").isEqualTo("R")
      assertThat(jsonContent).extractingJsonPathStringValue("[5].alertType").isEqualTo("R")
      assertThat(jsonContent).extractingJsonPathStringValue("[6].alertType").isEqualTo("R")
      assertThat(jsonContent).extractingJsonPathStringValue("[7].alertType").isEqualTo("R")
      assertThat(jsonContent).extractingJsonPathStringValue("[8].alertType").isEqualTo("V")
      assertThat(jsonContent).extractingJsonPathStringValue("[9].alertType").isEqualTo("V")
      assertThat(jsonContent).extractingJsonPathStringValue("[10].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathStringValue("[11].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathStringValue("[12].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathStringValue("[13].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathStringValue("[14].alertType").isEqualTo("X")
    }

    @Test
    @DisplayName("can change default sort order")
    fun canChangeSortOrder() {
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}/alerts/v2?sort={sort}&direction={direction}",
        GET,
        createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
        object : ParameterizedTypeReference<String?>() {
        },
        mapOf(
          "offenderNo" to "A1179MT",
          "sort" to "alertId",
          "direction" to "desc",
        ),
      )

      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(15)
      assertThat(jsonContent).extractingJsonPathNumberValue("[0].alertId").isEqualTo(12)
      assertThat(jsonContent).extractingJsonPathNumberValue("[1].alertId").isEqualTo(11)
      assertThat(jsonContent).extractingJsonPathNumberValue("[2].alertId").isEqualTo(10)
      assertThat(jsonContent).extractingJsonPathNumberValue("[3].alertId").isEqualTo(9)
      assertThat(jsonContent).extractingJsonPathNumberValue("[4].alertId").isEqualTo(8)
      assertThat(jsonContent).extractingJsonPathNumberValue("[5].alertId").isEqualTo(7)
      assertThat(jsonContent).extractingJsonPathNumberValue("[6].alertId").isEqualTo(6)
      assertThat(jsonContent).extractingJsonPathNumberValue("[7].alertId").isEqualTo(5)
      assertThat(jsonContent).extractingJsonPathNumberValue("[8].alertId").isEqualTo(4)
      assertThat(jsonContent).extractingJsonPathNumberValue("[9].alertId").isEqualTo(3)
      assertThat(jsonContent).extractingJsonPathNumberValue("[10].alertId").isEqualTo(3)
      assertThat(jsonContent).extractingJsonPathNumberValue("[11].alertId").isEqualTo(2)
      assertThat(jsonContent).extractingJsonPathNumberValue("[12].alertId").isEqualTo(2)
      assertThat(jsonContent).extractingJsonPathNumberValue("[13].alertId").isEqualTo(1)
      assertThat(jsonContent).extractingJsonPathNumberValue("[14].alertId").isEqualTo(1)
    }

    @Test
    @DisplayName("can filter by alert code")
    fun canFilterByAlertCode() {
      var jsonContent = getBodyAsJsonContent<Any>(
        testRestTemplate.exchange(
          "/api/offenders/{offenderNo}/alerts/v2?alertCodes={alertCodes}&sort=alertType,alertId",
          GET,
          createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
          object : ParameterizedTypeReference<String?>() {
          },
          mapOf(
            "offenderNo" to "A1179MT",
            "alertCodes" to "XTACT,XCU",
          ),
        ),
      )
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(5)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].alertCode").isEqualTo("XTACT")
      assertThat(jsonContent).extractingJsonPathStringValue("[1].alertCode").isEqualTo("XCU")
      assertThat(jsonContent).extractingJsonPathStringValue("[2].alertCode").isEqualTo("XTACT")
      assertThat(jsonContent).extractingJsonPathStringValue("[3].alertCode").isEqualTo("XCU")
      assertThat(jsonContent).extractingJsonPathStringValue("[4].alertCode").isEqualTo("XCU")

      jsonContent = getBodyAsJsonContent(
        testRestTemplate.exchange(
          "/api/offenders/{offenderNo}/bookings/latest/alerts?alertCodes={alertCodes}",
          GET,
          createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA),
          object : ParameterizedTypeReference<String?>() {
          },
          mapOf(
            "offenderNo" to "A1179MT",
            "alertCodes" to "XTACT",
          ),
        ),
      )
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("$").hasSize(1)
      assertThat(jsonContent).extractingJsonPathStringValue("[0].alertCode").isEqualTo("XTACT")
    }
  }
}
