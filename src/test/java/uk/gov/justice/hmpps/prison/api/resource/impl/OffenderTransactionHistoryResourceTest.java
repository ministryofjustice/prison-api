package uk.gov.justice.hmpps.prison.api.resource.impl;


import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

public class OffenderTransactionHistoryResourceTest extends ResourceTest {

    private final String OFFENDER_NUMBER = "123";
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQ = 400;


    @Test
    public void When_GetOffenderTransactionHistory_HappyPath() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                OFFENDER_NUMBER);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(0);
    }

    @Test
    public void When_GetOffenderTransactionHistory_WithMissingAccountCode() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {},
                OFFENDER_NUMBER);

        assertThatBodyStringAndStatus(response, "[]", HTTP_OK);
    }

    @Test
    public void When_GetOffenderTransactionHistory_WithFromAndToDates() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-04-01&to_date=2019-05-01";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {},
                OFFENDER_NUMBER);

        assertThatBodyStringAndStatus(response, "[]", HTTP_OK);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsCASH_Then_ReturnOneItem() {

        var offenderNumber = "-1001";
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=cash&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                offenderNumber);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeMissing_Then_ReturnOneItem() {

        var offenderNumber = "-1001";
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                offenderNumber);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(3);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsSAV_Then_ReturnOneItem() {

        var offenderNumber = "-1001";
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=savings&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                offenderNumber);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsSPND_Then_ReturnOneItem() {

        var offenderNumber = "-1001";
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                offenderNumber);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsREG_And_DifferentDate_Then_ReturnOneItem() {

        var offenderNumber = "-1001";
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=cash&from_date=2019-11-17&to_date=2019-11-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<OffenderTransactionHistoryDto>>() {},
                offenderNumber);

        assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_AccountCodeIsUnknown_Then_ErrorResponse() {

        var offenderNumber = "-1001";
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spendss&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<ErrorResponse>() {},
                offenderNumber);

        assertThat(response.getBody().getDeveloperMessage()).isEqualTo("Unknown account-code spendss");
        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_BAD_REQ);
        assertThat(response.getBody().getUserMessage()).isEqualTo("Unknown account-code spendss");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_OneRecordExisting_And_BadFromDateFormat_Then_ErrorResponse() {

        var offenderNumber = "-1001";
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-30-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<ErrorResponse>() {},
                offenderNumber);

        assertThat(response.getBody().getDeveloperMessage()).isEqualTo("Invalid value for MonthOfYear (valid values 1 - 12): 30");
        assertThat(response.getBody().getStatus().intValue()).isEqualTo(HTTP_BAD_REQ);
        assertThat(response.getBody().getUserMessage()).isEqualTo("Invalid value for MonthOfYear (valid values 1 - 12): 30");
    }

    @Test
    public void When_GetOffenderTransactionHistory_Then_ReturnCorrectJson() {

        var offenderNumber = "-1001";
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?account_code=spends&from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {},
                offenderNumber);

        assertThatJsonFileAndStatus(response, HTTP_OK,"When_GetOffenderTransactionHistory_Then_ReturnCorrectJson.json");
    }

    @Test
    public void When_GetOffenderTransactionHistory_And_MissingAccountCode_Then_ReturnCorrectJson() {

        var offenderNumber = "-1001";
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-10-17&to_date=2019-10-17";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {},
                offenderNumber);

        assertThatJsonFileAndStatus(response, HTTP_OK,"When_GetOffenderTransactionHistory_And_MissingAccountCode_Then_ReturnCorrectJson.json");
    }
}
