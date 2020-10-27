package uk.gov.justice.hmpps.prison.api.resource.impl;


import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OffenderTransactionHistoryResourceTest extends ResourceTest {

    private static final String NOMIS_ID = "A1114AA";

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
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                NOMIS_ID);

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
                new ParameterizedTypeReference<String>() {},
                NOMIS_ID);

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
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                NOMIS_ID);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
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
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                NOMIS_ID);

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
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                NOMIS_ID);

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
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                NOMIS_ID);

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
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                NOMIS_ID);

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
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                NOMIS_ID);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_FromDateIsMissing_And_ToDateIsNotToday_Then_ReturnError() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?to_date=2019-11-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<ErrorResponse>() {},
                NOMIS_ID);

        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_BAD_REQ);
        assertThat(response.getBody().getDeveloperMessage()).isEqualTo("toDate can't be before fromDate");
        assertThat(response.getBody().getUserMessage()).isEqualTo("toDate can't be before fromDate");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_FromDateIsMissing_And_ToDateIsToday_Then_NoErrorIsThrown() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?to_date=" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {},
                NOMIS_ID);
        
        assertThatJsonAndStatus(response, HTTP_OK, "[]");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_ToDateIsMissing_Then_ToDateIsAssumedToBeToday_And_OneRecordIsReturned() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-11-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                NOMIS_ID);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsMissing_And_DatesAreMissing_Then_ReturnOneItem() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                NOMIS_ID);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(0);
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
                new ParameterizedTypeReference<ErrorResponse>() {},
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
                new ParameterizedTypeReference<ErrorResponse>() {},
                NOMIS_ID);

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
                new ParameterizedTypeReference<ErrorResponse>() {},
                NOMIS_ID);

        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_BAD_REQ);
        assertThat(response.getBody().getDeveloperMessage()).isEqualTo("Invalid value for MonthOfYear (valid values 1 - 12): 30");
        assertThat(response.getBody().getUserMessage()).isEqualTo("Invalid value for MonthOfYear (valid values 1 - 12): 30");
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
                new ParameterizedTypeReference<String>() {},
                NOMIS_ID);

        assertThatJsonFileAndStatus(response, HTTP_OK,"When_GetOffenderTransactionHistory_Then_ReturnCorrectJson.json");
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
                new ParameterizedTypeReference<String>() {},
                NOMIS_ID);

        assertThatJsonFileAndStatus(response, HTTP_OK,"When_GetOffenderTransactionHistory_And_MissingAccountCode_Then_ReturnCorrectJson.json");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OffenderIdNotFound_Then_ErrorResponse() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17";

        var nonExistingId = "0";
        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<ErrorResponse>() {},
                nonExistingId);

        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_NOT_FOUND);
        assertThat(response.getBody().getDeveloperMessage()).isEqualTo("Resource with id [0] not found.");
        assertThat(response.getBody().getUserMessage()).isEqualTo("Resource with id [0] not found.");
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
                new ParameterizedTypeReference<ErrorResponse>() {},
                "Z00028");

        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_NOT_FOUND);
        assertThat(response.getBody().getDeveloperMessage()).isEqualTo("Resource with id [Z00028] not found.");
        assertThat(response.getBody().getUserMessage()).isEqualTo("Resource with id [Z00028] not found.");
    }
}
