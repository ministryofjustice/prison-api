@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA
import java.time.LocalDate

@DisplayName("BookingResource get offender alerts")
class BookingResourceIntTest_getOffenderAlerts : ResourceTest() {
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
  @DisplayName("GET /api/bookings/{bookingId}/alerts/v2")
  internal inner class NewSafeEndpoint {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/bookings/-56/alerts/v2")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/bookings/-56/alerts/v2")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 404 if booking does not exist`() {
      webTestClient.get().uri("/api/bookings/-99999/alerts/v2")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA `() {
      webTestClient.get().uri("/api/bookings/-1/alerts/v2")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("totalElements").isEqualTo(4)
        .jsonPath("content[*].alertCode").value<List<String>> { assertThat(it).containsExactlyInAnyOrder("XA", "HC", "RSS", "XTACT") }
    }

    @Test
    fun `returns 403 if not in user caseload`() {
      webTestClient.get().uri("/api/bookings/-3/alerts/v2")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Unauthorised access to booking with id -3.")
    }

    @Test
    @DisplayName("should have the correct role to access booking")
    fun shouldHaveTheCorrectRoleToAccessEndpoint() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2",
        HttpMethod.GET,
        createEmptyHttpEntity(AuthToken.RENEGADE_USER),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56),
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    @DisplayName("returns partial alert data for each alert")
    fun returnsImportantAlertDataForEachAlert() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56),
      )
      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("content").hasSize(10)

      // not returned when looking up by bookingId
      assertThat(jsonContent).extractingJsonPathNumberValue("content[0].bookingId").isNull()
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].offenderNo").isNull()
      assertThat(jsonContent).extractingJsonPathNumberValue("content[0].alertId").isEqualTo(10)
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertType").isEqualTo("R")
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertTypeDescription").isEqualTo("Risk")
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertCode").isEqualTo("RSS")
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertCodeDescription").isEqualTo("Risk to Staff - Custody")
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].comment").isEqualTo("Expired risk alert")
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateExpires").isEqualTo("2121-10-03")
      assertThat(jsonContent).extractingJsonPathBooleanValue("content[0].expired").isEqualTo(false)
      assertThat(jsonContent).extractingJsonPathBooleanValue("content[0].active").isEqualTo(false)
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].addedByFirstName").isEqualTo("API")
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].addedByLastName").isEqualTo("USER")
      // not returned when nobody has ever updated this alert
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].expiredByFirstName").isNull()
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].expiredByLastName").isNull()
    }

    @Test
    @DisplayName("expiredBy user is actually the last updated user and nothing to do with expiry")
    fun expiredByUserIsActuallyTheLastUpdatedUserAndNothingToDoWithExpiry() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56),
      )
      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("content").hasSize(10)
      assertThat(jsonContent).extractingJsonPathNumberValue("content[7].alertId").isEqualTo(1)
      assertThat(jsonContent).extractingJsonPathBooleanValue("content[7].expired").isEqualTo(false)
      assertThat(jsonContent).extractingJsonPathBooleanValue("content[7].active").isEqualTo(true)
      assertThat(jsonContent).extractingJsonPathStringValue("content[7].expiredByFirstName").isEqualTo("API")
      assertThat(jsonContent).extractingJsonPathStringValue("content[7].expiredByLastName").isEqualTo("USER")

      // not returned when nobody has ever updated this alert
      assertThat(jsonContent).extractingJsonPathStringValue("[0].expiredByFirstName").isNull()
      assertThat(jsonContent).extractingJsonPathStringValue("[0].expiredByLastName").isNull()
    }

    @Test
    @DisplayName("will return a page of alerts sorted by DESC by dateExpires,dateCreated which is ALERT_DATE not CREATE_DATETIME with no filters or sorting")
    fun willReturnAllActiveAlertsWhenNoFilter() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56),
      )
      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("content").hasSize(10)
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateExpires").isEqualTo("2121-10-03")
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[1].dateExpires").isEqualTo("2121-10-02")
      assertThat(jsonContent).extractingJsonPathStringValue("content[1].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[2].dateExpires").isEqualTo("2121-10-01")
      assertThat(jsonContent).extractingJsonPathStringValue("content[2].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[3].dateExpires").isEqualTo("2121-09-03")
      assertThat(jsonContent).extractingJsonPathStringValue("content[3].dateCreated").isEqualTo("2021-08-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[4].dateExpires").isEqualTo("2121-09-03")
      assertThat(jsonContent).extractingJsonPathStringValue("content[4].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[5].dateExpires").isEqualTo("2121-09-03")
      assertThat(jsonContent).extractingJsonPathStringValue("content[5].dateCreated").isEqualTo("2021-06-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[6].dateExpires").isEqualTo("2121-09-02")
      assertThat(jsonContent).extractingJsonPathStringValue("content[6].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[7].dateExpires").isEqualTo("2121-09-01")
      assertThat(jsonContent).extractingJsonPathStringValue("content[7].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[8].dateExpires").isEqualTo("2021-08-04")
      assertThat(jsonContent).extractingJsonPathStringValue("content[8].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[9].dateExpires").isEqualTo("2021-08-03")
      assertThat(jsonContent).extractingJsonPathStringValue("content[9].dateCreated").isEqualTo("2021-07-19")
    }

    @Test
    @DisplayName("can control sort order")
    fun canControlSortOrder() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?sort={sort}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "sort" to "dateCreated,desc"),
      )
      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("content").hasSize(10)
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-08-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[1].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[2].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[3].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[4].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[5].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[6].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[7].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[8].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[9].dateCreated").isEqualTo("2021-07-19")
    }

    @Test
    @DisplayName("page totals are returned in the response body")
    fun pageTotalsAreReturnedInTheResponseHeaders() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56),
      )
      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathNumberValue("totalElements").isEqualTo(12)
      assertThat(jsonContent).extractingJsonPathNumberValue("numberOfElements").isEqualTo(10)
      assertThat(jsonContent).extractingJsonPathNumberValue("number").isEqualTo(0)
      assertThat(jsonContent).extractingJsonPathNumberValue("totalPages").isEqualTo(2)
      assertThat(jsonContent).extractingJsonPathNumberValue("size").isEqualTo(10)
    }

    @Test
    @DisplayName("can request second page of results")
    fun canRequestSecondPageOfResults() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?page={page}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "page" to 1),
      )
      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("content").hasSize(2)
      assertThat(jsonContent).extractingJsonPathNumberValue("numberOfElements").isEqualTo(2)
      assertThat(jsonContent).extractingJsonPathNumberValue("totalElements").isEqualTo(12)
      assertThat(jsonContent).extractingJsonPathNumberValue("number").isEqualTo(1)
      assertThat(jsonContent).extractingJsonPathNumberValue("totalPages").isEqualTo(2)
      assertThat(jsonContent).extractingJsonPathNumberValue("size").isEqualTo(10)
    }

    @Test
    @DisplayName("can filter by alert type")
    fun canFilterByAlertType() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?alertType={alertType}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "alertType" to "X"),
      )
      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("content").hasSize(3)
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertCode").isEqualTo("XCU")
      assertThat(jsonContent).extractingJsonPathStringValue("content[1].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathStringValue("content[1].alertCode").isEqualTo("XCU")
      assertThat(jsonContent).extractingJsonPathStringValue("content[2].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathStringValue("content[2].alertCode").isEqualTo("XTACT")
    }

    @Test
    @DisplayName("can filter by active state")
    fun canFilterByActiveState() {
      val activeResponse = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?alertStatus={alertStatus}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "alertStatus" to "ACTIVE"),
      )
      assertThat(getBodyAsJsonContent<Any>(activeResponse)).extractingJsonPathArrayValue<Any>("content").hasSize(9)
      val inactiveResponse = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?alertStatus={alertStatus}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "alertStatus" to "INACTIVE"),
      )
      assertThat(getBodyAsJsonContent<Any>(inactiveResponse)).extractingJsonPathArrayValue<Any>("content").hasSize(3)
    }

    @Test
    @DisplayName("can filter by before createDate (alert date)")
    fun canFilterByBeforeCreateDate() {
      val oldestAlertResponse = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?to={to}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "to" to "2021-06-19"),
      )
      assertThat(getBodyAsJsonContent<Any>(oldestAlertResponse)).extractingJsonPathArrayValue<Any>("content").hasSize(1)
      assertThat(getBodyAsJsonContent<Any>(oldestAlertResponse)).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-06-19")
      val justBeforeSecondOldestAlertResponse = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?to={to}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "to" to "2021-07-18"),
      )
      assertThat(getBodyAsJsonContent<Any>(justBeforeSecondOldestAlertResponse)).extractingJsonPathArrayValue<Any>("content").hasSize(1)
      assertThat(getBodyAsJsonContent<Any>(justBeforeSecondOldestAlertResponse)).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-06-19")
    }

    @Test
    @DisplayName("can filter by after createDate (alert date)")
    fun canFilterByAfterCreateDate() {
      val youngestAlertResponse = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?from={from}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "from" to "2021-08-19"),
      )
      assertThat(getBodyAsJsonContent<Any>(youngestAlertResponse)).extractingJsonPathArrayValue<Any>("content").hasSize(1)
      assertThat(getBodyAsJsonContent<Any>(youngestAlertResponse)).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-08-19")
      val justAfterSecondYoungestAlertResponse = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?from={from}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "from" to "2021-07-20"),
      )
      assertThat(getBodyAsJsonContent<Any>(justAfterSecondYoungestAlertResponse)).extractingJsonPathArrayValue<Any>("content").hasSize(1)
      assertThat(getBodyAsJsonContent<Any>(justAfterSecondYoungestAlertResponse)).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-08-19")
    }

    @Test
    @DisplayName("can filter between inclusive two dates")
    fun canFilterBetweenInclusiveTwoDates() {
      val middleDatesResponse = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?from={from}&to={to}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "from" to "2021-07-19", "to" to "2021-07-19"),
      )
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathArrayValue<Any>("content").hasSize(10)
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathNumberValue("totalElements").isEqualTo(10)
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-07-19")
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathStringValue("content[1].dateCreated").isEqualTo("2021-07-19")
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathStringValue("content[2].dateCreated").isEqualTo("2021-07-19")
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathStringValue("content[3].dateCreated").isEqualTo("2021-07-19")
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathStringValue("content[4].dateCreated").isEqualTo("2021-07-19")
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathStringValue("content[5].dateCreated").isEqualTo("2021-07-19")
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathStringValue("content[6].dateCreated").isEqualTo("2021-07-19")
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathStringValue("content[7].dateCreated").isEqualTo("2021-07-19")
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathStringValue("content[8].dateCreated").isEqualTo("2021-07-19")
      assertThat(getBodyAsJsonContent<Any>(middleDatesResponse)).extractingJsonPathStringValue("content[9].dateCreated").isEqualTo("2021-07-19")
      val justBeforeAndAfterMiddleDatesResponse = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?from={from}&to={to}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "from" to "2021-07-18", "to" to "2021-07-20"),
      )
      assertThat(getBodyAsJsonContent<Any>(justBeforeAndAfterMiddleDatesResponse)).extractingJsonPathArrayValue<Any>("content").hasSize(10)
      assertThat(getBodyAsJsonContent<Any>(justBeforeAndAfterMiddleDatesResponse)).extractingJsonPathNumberValue("totalElements").isEqualTo(10)
    }

    @Test
    @DisplayName("can filter be dateCreated and active state and alert type")
    fun canFilterBeDateCreatedAndActiveStateAndAlertType() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?alertType={alertType}&from={from}&to={to}&alertStatus={alertStatus}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "from" to "2021-07-19", "to" to "2021-07-19", "alertType" to "X", "alertStatus" to "INACTIVE"),
      )
      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("content").hasSize(1)
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].dateCreated").isEqualTo("2021-07-19")
      assertThat(jsonContent).extractingJsonPathStringValue("content[0].alertType").isEqualTo("X")
      assertThat(jsonContent).extractingJsonPathBooleanValue("content[0].active").isFalse()
    }

    @Test
    @DisplayName("empty filter parameters are ignored")
    fun emptyFilterParametersAreIgnored() {
      val response = testRestTemplate.exchange(
        "/api/bookings/{bookingId}/alerts/v2?alertType={alertType}&from={from}&to={to}&alertStatus={alertStatus}",
        HttpMethod.GET,
        createEmptyHttpEntity(VIEW_PRISONER_DATA, mapOf()),
        object : ParameterizedTypeReference<String?>() {},
        mapOf("bookingId" to -56, "from" to "", "to" to "", "alertType" to "", "alertStatus" to ""),
      )
      val jsonContent = getBodyAsJsonContent<Any>(response)
      assertThat(jsonContent).extractingJsonPathArrayValue<Any>("content").hasSize(10)
      assertThat(jsonContent).extractingJsonPathNumberValue("totalElements").isEqualTo(12)
    }
  }

  @DisplayName("POST /api/bookings/offenderNo/{agencyId}/alerts")
  @Nested
  inner class OffenderAlertsByAgency {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.post().uri("/api/bookings/offenderNo/LEI/alerts")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\" ]")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client has no override role`() {
      webTestClient.post().uri("/api/bookings/offenderNo/LEI/alerts")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\" ]")
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when client has override role ROLE_SYSTEM_USER`() {
      webTestClient.post().uri("/api/bookings/offenderNo/LEI/alerts")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\", \"A1234AF\" ]")
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_GlOBAL_SEARCH`() {
      webTestClient.post().uri("/api/bookings/offenderNo/LEI/alerts")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\", \"A1234AF\" ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(6)
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.post().uri("/api/bookings/offenderNo/LEI/alerts")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\"]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(4)
    }

    @Test
    fun `returns 403 if not in user caseload`() {
      webTestClient.post().uri("/api/bookings/offenderNo/LEI/alerts")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("""["A1234AA","A1234AF"]""")
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success if  in user caseload`() {
      val today = LocalDate.now().toString()
      webTestClient.post().uri("/api/bookings/offenderNo/LEI/alerts")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("""["A1234AA","A1234AF"]""")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(6)
        .jsonPath("[*].offenderNo").value<List<String>> { assertThat(it).containsExactly("A1234AF", "A1234AF", "A1234AA", "A1234AA", "A1234AA", "A1234AA") }
        .jsonPath("[*].bookingId").value<List<Int>> { assertThat(it).containsExactly(-6, -6, -1, -1, -1, -1) }
        .jsonPath("[*].alertId").value<List<Int>> { assertThat(it).containsExactly(1, 2, 1, 2, 3, 4) }
        .jsonPath("[*].alertType").value<List<String>> { assertThat(it).containsExactly("P", "X", "X", "H", "R", "X") }
        .jsonPath("[*].alertCode").value<List<String>> { assertThat(it).containsExactly("P1", "XTACT", "XA", "HC", "RSS", "XTACT") }
        .jsonPath("[0].dateCreated").isEqualTo(today)
        .jsonPath("[4].dateCreated").isEqualTo("2020-06-01")
        .jsonPath("[0].alertCodeDescription").isEqualTo("MAPPA Level 1 Case")
        .jsonPath("[4].alertCodeDescription").isEqualTo("Risk to Staff - Custody")
        .jsonPath("[0].dateExpires").doesNotExist()
        .jsonPath("[4].dateExpires").isEqualTo("2020-06-01")
        .jsonPath("[0].expired").isEqualTo(false)
        .jsonPath("[4].expired").isEqualTo(true)
        .jsonPath("[0].active").isEqualTo(true)
        .jsonPath("[4].active").isEqualTo(false)
        .jsonPath("[0].modifiedDateTime").doesNotExist()
        .jsonPath("[4].modifiedDateTime").isEqualTo("2006-12-10T03:52:25")
    }
  }
}
