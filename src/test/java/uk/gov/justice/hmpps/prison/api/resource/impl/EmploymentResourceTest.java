package uk.gov.justice.hmpps.prison.api.resource.impl;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.service.OffenderEmploymentService;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;


public class EmploymentResourceTest extends ResourceTest {

    private final String OFFENDER_NUMBER = "A1234AB";

    @MockBean
    private OffenderEmploymentService offenderEmploymentService;

    @Test
    public void testShouldNotBeAbleToAccessInformation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/employment/prisoner/{offenderNo}",
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            OFFENDER_NUMBER);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testShouldBeAbleToAccessInformationAsASystemUser() {

        when(offenderEmploymentService.getOffenderEmployments(OFFENDER_NUMBER, PageRequest.of(0, 10))).thenReturn(new PageImpl<>(emptyList()));

        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/employment/prisoner/{offenderNo}",
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            OFFENDER_NUMBER);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void testShouldBeAbleToAccessInformationAsAGlobalSearchUser() {

        when(offenderEmploymentService.getOffenderEmployments(OFFENDER_NUMBER, PageRequest.of(0, 10))).thenReturn(new PageImpl<>(emptyList()));

        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/employment/prisoner/{offenderNo}",
            GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {},
            OFFENDER_NUMBER);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }
}