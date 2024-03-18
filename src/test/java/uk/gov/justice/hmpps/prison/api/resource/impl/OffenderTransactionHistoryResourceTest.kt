package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER

class OffenderTransactionHistoryResourceTest : ResourceTest() {

  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.get().uri("/api/offenders/$OFFENDER_NO/transaction-history")
      .exchange().expectStatus().isUnauthorized
  }

  @Test
  fun `returns 403 when client does not have any roles`() {
    webTestClient.get().uri("/api/offenders/$OFFENDER_NO/transaction-history")
      .headers(setClientAuthorisation(listOf()))
      .exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 403 if not in user caseload`() {
    webTestClient.get().uri("/api/offenders/$OFFENDER_NO/transaction-history")
      .headers(setAuthorisation("WAI_USER", listOf())).exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 403 if user has no caseloads`() {
    webTestClient.get().uri("/api/offenders/$OFFENDER_NO/transaction-history")
      .headers(setAuthorisation("RO_USER", listOf())).exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 404 if client has override role and booking does not exist`() {
    webTestClient.get().uri("/api/offenders/-99999/transaction-history")
      .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA"))).exchange().expectStatus().isNotFound
  }

  @Test
  fun `returns 404 if client does not have override role and booking does not exist`() {
    webTestClient.get().uri("/api/offenders/-99999/transaction-history")
      .headers(setClientAuthorisation(listOf())).exchange().expectStatus().isNotFound
  }

  @Test
  fun `returns 404 if user has caseloads and booking does not exist`() {
    webTestClient.get().uri("/api/offenders/-99999/transaction-history")
      .headers(setAuthorisation("ITAG_USER", listOf())).exchange().expectStatus().isNotFound
  }

  @Test
  fun `returns 404 if user does not have any caseloads and booking does not exist`() {
    webTestClient.get().uri("/api/offenders/-99999/transaction-history")
      .headers(setAuthorisation("RO_USER", listOf())).exchange().expectStatus().isNotFound
  }

  @Test
  fun `returns success when client has GLOBAL_SEARCH override role`() {
    webTestClient.get().uri("/api/offenders/$OFFENDER_NO/transaction-history")
      .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
      .exchange().expectStatus().isOk
  }

  @Test
  fun `returns success when client has VIEW_PRISONER_DATA override role`() {
    webTestClient.get().uri("/api/offenders/$OFFENDER_NO/transaction-history")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange().expectStatus().isOk
  }

  @Test
  fun whenGetOffenderTransactionHistoryHappyPath() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-04-01&to_date=2019-05-01"

    val response: ResponseEntity<List<OffenderTransactionHistoryDto>> =
      testRestTemplate.exchange(
        url,
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
        },
        OFFENDER_NO,
      )

    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body).isInstanceOf(MutableList::class.java)
    assertThat(response.body!!.size).isEqualTo(0)
  }

  @Test
  fun whenGetOffenderTransactionHistoryWithMissingAccountCode() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-04-01&to_date=2019-05-01"

    val response = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {
      },
      OFFENDER_NO,
    )

    assertThatJsonAndStatus(response, HTTP_OK, "[]")
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsCASH_Then_ReturnOneItem() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=cash&from_date=2019-10-17&to_date=2019-10-17"

    val response = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      String::class.java,
      OFFENDER_NO,
    )

    assertThatJsonFileAndStatus(response, 200, "offender-transaction-history.json")
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_ThreeRecordsExist_And_AccountCodeMissing_Then_ReturnThreeItems() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-10-17&to_date=2019-10-17"

    val response: ResponseEntity<List<OffenderTransactionHistoryDto>> = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
      },
      OFFENDER_NO,
    )

    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body).isInstanceOf(MutableList::class.java)
    assertThat(response.body!!.size).isEqualTo(3)
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsSAV_Then_ReturnOneItem() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=savings&from_date=2019-10-17&to_date=2019-10-17"

    val response: ResponseEntity<List<OffenderTransactionHistoryDto>> = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
      },
      OFFENDER_NO,
    )

    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body).isInstanceOf(MutableList::class.java)
    assertThat(response.body!!.size).isEqualTo(1)
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsSPND_Then_ReturnOneItem() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17"

    val response: ResponseEntity<List<OffenderTransactionHistoryDto>> = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
      },
      OFFENDER_NO,
    )

    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body).isInstanceOf(MutableList::class.java)
    assertThat(response.body!!.size).isEqualTo(1)
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsREG_And_DifferentDate_Then_ReturnOneItem() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=cash&from_date=2019-11-17&to_date=2019-11-17"

    val response: ResponseEntity<List<OffenderTransactionHistoryDto>> = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
      },
      OFFENDER_NO,
    )

    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body).isInstanceOf(MutableList::class.java)
    assertThat(response.body!!.size).isEqualTo(1)
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsMissing_And_DifferentDate_Then_ReturnOneItem() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-11-17&to_date=2019-11-17"

    val response: ResponseEntity<List<OffenderTransactionHistoryDto>> = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
      },
      OFFENDER_NO,
    )

    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body).isInstanceOf(MutableList::class.java)
    assertThat(response.body!!.size).isEqualTo(1)
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_BadOffenderId_Then_ErrorResponse() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=spendss&from_date=2019-10-17&to_date=2019-10-17"

    val responseBody = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<ErrorResponse?>() {
      },
      "xxx",
    ).body!!

    assertThat(responseBody.status).isEqualTo(HTTP_NOT_FOUND)
    assertThat(responseBody.developerMessage).isEqualTo("Resource with id [xxx] not found.")
    assertThat(responseBody.userMessage).isEqualTo("Resource with id [xxx] not found.")
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_AccountCodeIsUnknown_Then_ErrorResponse() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=spendss&from_date=2019-10-17&to_date=2019-10-17"

    val responseBody = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<ErrorResponse?>() {
      },
      OFFENDER_NO,
    ).body!!

    assertThat(responseBody.status).isEqualTo(HTTP_BAD_REQ)
    assertThat(responseBody.developerMessage).isEqualTo("Unknown account-code spendss")
    assertThat(responseBody.userMessage).isEqualTo("Unknown account-code spendss")
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_BadFromDateFormat_Then_ErrorResponse() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-30-17&to_date=2019-10-17"

    val responseBody = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<ErrorResponse?>() {
      },
      OFFENDER_NO,
    ).body!!

    assertThat(responseBody.status).isEqualTo(HTTP_BAD_REQ)
    assertThat(responseBody.developerMessage).startsWith("Invalid value for MonthOfYear (valid values 1 - 12): 30")
    assertThat(responseBody.userMessage).startsWith("Invalid value for MonthOfYear (valid values 1 - 12): 30")
  }

  @Test
  fun whenGetOffenderTransactionHistory_Then_ReturnCorrectJson() {
    webTestClient.get().uri("/api/offenders/$OFFENDER_NO/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17")
      .headers(setAuthorisation("ITAG_USER", listOf()))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("When_GetOffenderTransactionHistory_Then_ReturnCorrectJson.json".readFile())
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_MissingAccountCode_Then_ReturnCorrectJson() {
    webTestClient.get().uri("/api/offenders/$OFFENDER_NO/transaction-history?from_date=2019-10-17&to_date=2019-10-17")
      .headers(setAuthorisation("ITAG_USER", listOf()))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("When_GetOffenderTransactionHistory_And_MissingAccountCode_Then_ReturnCorrectJson.json".readFile())
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_OffenderIdNotFound_Then_ErrorResponse() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17"

    val nonExistingId = "nonExistingId"
    val responseBody = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<ErrorResponse?>() {
      },
      nonExistingId,
    ).body!!

    assertThat(responseBody.status).isEqualTo(HTTP_NOT_FOUND)
    assertThat(responseBody.developerMessage).isEqualTo("Resource with id [nonExistingId] not found.")
    assertThat(responseBody.userMessage).isEqualTo("Resource with id [nonExistingId] not found.")
  }

  @Test
  fun whenGetOffenderTransactionHistory_And_OffenderInDifferentCaseLoad_Then_ReturnCorrectJson() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17"

    val responseBody = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<ErrorResponse?>() {
      },
      "Z00028",
    ).body!!

    assertThat(responseBody.status).isEqualTo(HTTP_NOT_FOUND)
    assertThat(responseBody.developerMessage).isEqualTo("Resource with id [Z00028] not found.")
    assertThat(responseBody.userMessage).isEqualTo("Resource with id [Z00028] not found.")
  }

  @Test
  fun whenGetOffenderTransactionHistory_With_Related_Transactions() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=cash&from_date=2000-10-17&to_date=2001-10-17"

    val response = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      String::class.java,
      "A1234AJ",
    )

    assertThatJsonFileAndStatus(response, 200, "offender-transaction-history-with-related.json")
  }

  @Test
  fun whenGetOffenderTransactionHistory_With_No_DateRange_Supplied_And_All_Accounts() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)

    val url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends"

    val response = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      String::class.java,
      "A1234AJ",
    )

    assertThatJsonFileAndStatus(response, 200, "all-spends-transactions.json")
  }

  @Test
  fun whenGetOffenderTransactionHistory_With_TransactionType() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)

    val url = "/api/offenders/{offenderNo}/transaction-history?transaction_type=A_EARN"

    val response = testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      httpEntity,
      String::class.java,
      "A1234AJ",
    )

    assertThatJsonFileAndStatus(response, 200, "a-earn-transaction-history.json")
  }

  @Test
  fun whenGetOffenderTransactionHistory_called_with_no_Role() {
    webTestClient.get()
      .uri("/api/offenders/{offenderNo}/transaction-history", OFFENDER_NO)
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
  }

  companion object {
    private const val OFFENDER_NO = "A1234AI"

    private val HTTP_OK = OK.value()
    private val HTTP_BAD_REQ = HttpStatus.BAD_REQUEST.value()
    private val HTTP_NOT_FOUND = HttpStatus.NOT_FOUND.value()
  }

  internal fun String.readFile(): String = this@OffenderTransactionHistoryResourceTest::class.java.getResource(this)!!.readText()
}
