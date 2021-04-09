package uk.gov.justice.hmpps.prison.api.resource.impl;


import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderTransactionHistoryResourceTest extends ResourceTest {

    private static final String OFFENDER_NO = "A1234AI";

    private static final int HTTP_OK = HttpStatus.OK.value();
    private static final int HTTP_BAD_REQ = HttpStatus.BAD_REQUEST.value();
    private static final int HTTP_NOT_FOUND = HttpStatus.NOT_FOUND.value();

    @Test
    public void When_GetOffenderTransactionHistory_HappyPath() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-04-01&to_date=2019-05-01";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
            },
            OFFENDER_NO);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(0);
    }

    @Test
    public void When_GetOffenderTransactionHistory_WithMissingAccountCode() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-04-01&to_date=2019-05-01";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            OFFENDER_NO);

        assertThatJsonAndStatus(response, HTTP_OK, "[]");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsCASH_Then_ReturnOneItem() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=cash&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            String.class,
            OFFENDER_NO);

        assertThatJsonFileAndStatus(response, 200, "offender-transaction-history.json");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_ThreeRecordsExist_And_AccountCodeMissing_Then_ReturnThreeItems() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
            },
            OFFENDER_NO);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(3);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsSAV_Then_ReturnOneItem() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=savings&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
            },
            OFFENDER_NO);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsSPND_Then_ReturnOneItem() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
            },
            OFFENDER_NO);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsREG_And_DifferentDate_Then_ReturnOneItem() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=cash&from_date=2019-11-17&to_date=2019-11-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
            },
            OFFENDER_NO);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsMissing_And_DifferentDate_Then_ReturnOneItem() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-11-17&to_date=2019-11-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {
            },
            OFFENDER_NO);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_BadOffenderId_Then_ErrorResponse() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spendss&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<ErrorResponse>() {
            },
            "xxx");

        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_NOT_FOUND);
        assertThat(response.getBody().getDeveloperMessage()).isEqualTo("Resource with id [xxx] not found.");
        assertThat(response.getBody().getUserMessage()).isEqualTo("Resource with id [xxx] not found.");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_AccountCodeIsUnknown_Then_ErrorResponse() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spendss&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<ErrorResponse>() {
            },
            OFFENDER_NO);

        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_BAD_REQ);
        assertThat(response.getBody().getDeveloperMessage()).isEqualTo("Unknown account-code spendss");
        assertThat(response.getBody().getUserMessage()).isEqualTo("Unknown account-code spendss");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_BadFromDateFormat_Then_ErrorResponse() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-30-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<ErrorResponse>() {
            },
            OFFENDER_NO);

        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_BAD_REQ);
        assertThat(response.getBody().getDeveloperMessage()).startsWith("Unable to parse date time value \"2019-30-17\"");
        assertThat(response.getBody().getUserMessage()).startsWith("Unable to parse date time value \"2019-30-17\"");
    }

    @Test
    public void When_GetOffenderTransactionHistory_Then_ReturnCorrectJson() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            OFFENDER_NO);

        assertThatJsonFileAndStatus(response, HTTP_OK, "When_GetOffenderTransactionHistory_Then_ReturnCorrectJson.json");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_MissingAccountCode_Then_ReturnCorrectJson() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            },
            OFFENDER_NO);

        assertThatJsonFileAndStatus(response, HTTP_OK, "When_GetOffenderTransactionHistory_And_MissingAccountCode_Then_ReturnCorrectJson.json");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OffenderIdNotFound_Then_ErrorResponse() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17";

        var nonExistingId = "nonExistingId";
        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<ErrorResponse>() {
            },
            nonExistingId);

        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_NOT_FOUND);
        assertThat(response.getBody().getDeveloperMessage()).isEqualTo("Resource with id [nonExistingId] not found.");
        assertThat(response.getBody().getUserMessage()).isEqualTo("Resource with id [nonExistingId] not found.");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OffenderInDifferentCaseLoad_Then_ReturnCorrectJson() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<ErrorResponse>() {
            },
            "Z00028");

        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_NOT_FOUND);
        assertThat(response.getBody().getDeveloperMessage()).isEqualTo("Resource with id [Z00028] not found.");
        assertThat(response.getBody().getUserMessage()).isEqualTo("Resource with id [Z00028] not found.");
    }

    @Test
    public void When_GetOffenderTransactionHistory_With_Related_Transactions() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=cash&from_date=2000-10-17&to_date=2001-10-17";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            String.class,
            "A1234AJ");

        assertThatJsonFileAndStatus(response, 200, "offender-transaction-history-with-related.json");
    }

    @Test
    public void When_GetOffenderTransactionHistory_With_No_DateRange_Supplied_And_All_Accounts() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            String.class,
            "A1234AJ");

        assertThatJsonFileAndStatus(response, 200, "all-spends-transactions.json");
    }

    @Test
    public void When_GetOffenderTransactionHistory_With_TransactionType() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var url = "/api/offenders/{offenderNo}/transaction-history?transaction_type=A_EARN";

        final var response = testRestTemplate.exchange(
            url,
            HttpMethod.GET,
            httpEntity,
            String.class,
            "A1234AJ");

        assertThatJsonFileAndStatus(response, 200, "a-earn-transaction-history.json");
    }
}
