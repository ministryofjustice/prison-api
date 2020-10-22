package uk.gov.justice.hmpps.prison.api.resource.impl;


import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

public class OffenderTransactionHistoryResourceTest extends ResourceTest {

    private final String OFFENDER_NUMBER = "123";

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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getBody()).isEqualTo("[]");
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getBody()).isEqualTo("[]");
    }

    //@Test
    public void When_GetOffenderTransactionHistory_WithMissingOffenderId() {

        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var url = "/api/offenders/{offenderNo}/transaction-history?from_date=2019-04-01&to_date=2019-05-01";

        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        //assertThat(response.getBody()).isEqualTo("[]");
    }
}
