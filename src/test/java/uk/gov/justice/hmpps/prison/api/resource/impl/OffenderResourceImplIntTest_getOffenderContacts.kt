@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.json.JsonContent
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken

class OffenderResourceImplIntTest_getOffenderContacts : ResourceTest() {
  @Test
  fun shouldReturnListOfContacts() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)

    val request = createHttpEntity(token, null)

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/offenders/A1234AH/contacts",
      HttpMethod.GET,
      request,
      object : ParameterizedTypeReference<String>() {
      },
    )

    val json: JsonContent<Any> = getBodyAsJsonContent(response)
    assertThat(json).extractingJsonPathArrayValue<Any>("offenderContacts").hasSize(3)
    assertThat(json).extractingJsonPathStringValue("offenderContacts[0].lastName")
      .isEqualTo("Johnson")
    assertThat(json).extractingJsonPathStringValue("offenderContacts[0].firstName")
      .isEqualTo("John")
    assertThat(json).extractingJsonPathStringValue("offenderContacts[0].middleName")
      .isEqualTo("Justice")
    assertThat(json).extractingJsonPathStringValue("offenderContacts[0].contactType")
      .isEqualTo("S")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].contactTypeDescription").isEqualTo("Social/Family")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].relationshipCode").isEqualTo("FRI")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].emails[0].email").isEqualTo("visitor@other.com")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].relationshipDescription").isEqualTo("Friend")
    assertThat(json).extractingJsonPathNumberValue("offenderContacts[0].bookingId")
      .isEqualTo(-8)
    assertThat(json).extractingJsonPathNumberValue("offenderContacts[0].personId")
      .isEqualTo(-3)
    assertThat(json).extractingJsonPathBooleanValue("offenderContacts[0].nextOfKin")
      .isTrue()
    assertThat(json)
      .extractingJsonPathBooleanValue("offenderContacts[0].emergencyContact").isTrue()
    assertThat(json)
      .extractingJsonPathBooleanValue("offenderContacts[0].approvedVisitor").isTrue()
    assertThat(json)
      .extractingJsonPathNumberValue("offenderContacts[0].restrictions[0].restrictionId").isEqualTo(13520)
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].restrictions[0].comment").isEqualTo("a comment")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].restrictions[0].restrictionType").isEqualTo("CLOSED")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].restrictions[0].restrictionTypeDescription")
      .isEqualTo("Closed")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].restrictions[0].startDate").isEqualTo("2021-10-15")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].restrictions[0].expiryDate").isEqualTo("2026-10-13")
    assertThat(json)
      .extractingJsonPathBooleanValue("offenderContacts[0].restrictions[0].globalRestriction").isTrue()
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].restrictions[1].comment").isEqualTo("Some Comment Text")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].restrictions[1].restrictionType").isEqualTo("BAN")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].restrictions[1].restrictionTypeDescription")
      .isEqualTo("Banned")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].restrictions[1].startDate").isEqualTo("2001-01-01")
    assertThat(json)
      .extractingJsonPathStringValue("offenderContacts[0].restrictions[1].expiryDate").isNull()
    assertThat(json)
      .extractingJsonPathNumberValue("offenderContacts[0].restrictions[1].restrictionId").isEqualTo(-2)
    assertThat(json)
      .extractingJsonPathBooleanValue("offenderContacts[0].restrictions[1].globalRestriction").isFalse()
    assertThat(json).extractingJsonPathBooleanValue("offenderContacts[0].active")
      .isTrue()
    assertThat(json)
      .extractingJsonPathBooleanValue("offenderContacts[1].approvedVisitor").isFalse()
    assertThat(json).extractingJsonPathBooleanValue("offenderContacts[1].active")
      .isTrue()
    assertThat(json)
      .extractingJsonPathArrayValue<Any>("offenderContacts[1].restrictions").isEmpty()
    assertThat(json).extractingJsonPathBooleanValue("offenderContacts[2].active")
      .isFalse()
  }

  @Test
  fun shouldReturnVisitorApprovedListOfContacts() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)

    val request = createHttpEntity(token, null)

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/offenders/A1234AH/contacts?approvedVisitorsOnly=true",
      HttpMethod.GET,
      request,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathArrayValue<Any>("offenderContacts").hasSize(1)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathNumberValue("offenderContacts[0].personId").isEqualTo(-3)
  }

  @Test
  fun shouldReturnListOfActiveContacts() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)

    val request = createHttpEntity(token, null)

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/offenders/A1234AH/contacts?activeOnly=true",
      HttpMethod.GET,
      request,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    ).extractingJsonPathArrayValue<Any>("offenderContacts")
      .extracting("active").containsExactly(true, true)
  }

  @Test
  fun shouldReturn404WhenOffenderNotFound() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)

    val request = createHttpEntity(token, null)

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/offenders/A1774AA/contacts",
      HttpMethod.GET,
      request,
      ErrorResponse::class.java,
    )

    assertThat<ErrorResponse>(response.getBody()).isEqualTo(
      ErrorResponse.builder()
        .status(404)
        .userMessage("Resource with id [A1774AA] not found.")
        .developerMessage("Resource with id [A1774AA] not found.")
        .build(),
    )
  }

  @Test
  fun shouldReturn403IfNotAuthorised() {
    val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)

    val request = createHttpEntity(token, null)

    val response: ResponseEntity<ErrorResponse> = testRestTemplate.exchange(
      "/api/offenders/A1234AH/contacts",
      HttpMethod.GET,
      request,
      ErrorResponse::class.java,
    )

    assertThat(response.getBody()?.status).isEqualTo(403)
  }
}
