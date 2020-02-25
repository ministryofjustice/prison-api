package uk.gov.justice.hmpps.nomis.datacompliance.controller;

import net.syscon.elite.api.model.PendingDeletionRequest;
import net.syscon.elite.api.resource.impl.ResourceTest;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDataComplianceService;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;

import static net.syscon.elite.executablespecification.steps.AuthTokenHelper.AuthToken.ELITE2_API_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.http.HttpStatus.ACCEPTED_202;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpMethod.POST;

public class DataComplianceControllerTest extends ResourceTest {

    private static final String REQUEST_ID = "123";
    private static final LocalDateTime WINDOW_START = LocalDateTime.now();
    private static final LocalDateTime WINDOW_END = WINDOW_START.plusDays(1);

    @MockBean
    private OffenderDataComplianceService offenderDataComplianceService;

    @Test
    public void requestOffenderPendingDeletions() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(ELITE2_API_USER),
                PendingDeletionRequest.builder()
                        .requestId(REQUEST_ID)
                        .dueForDeletionWindowStart(WINDOW_START)
                        .dueForDeletionWindowEnd(WINDOW_END)
                        .build());

        final var response = testRestTemplate.exchange("/api/data-compliance/offenders/pending-deletions", POST, requestEntity, Void.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(ACCEPTED_202);
        verify(offenderDataComplianceService).acceptOffendersPendingDeletionRequest(REQUEST_ID, WINDOW_START, WINDOW_END);
    }

    @Test
    public void requestOffenderPendingDeletionsExpectsBodyNotNull() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(ELITE2_API_USER), null);

        final var response = testRestTemplate.exchange("/api/data-compliance/offenders/pending-deletions", POST, requestEntity, Void.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(BAD_REQUEST_400);
    }

    @Test
    public void requestOffenderPendingDeletionsExpectsDeletionWindowEnd() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(ELITE2_API_USER),
                PendingDeletionRequest.builder().build());

        final var response = testRestTemplate.exchange("/api/data-compliance/offenders/pending-deletions", POST, requestEntity, Void.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(BAD_REQUEST_400);
    }
}
