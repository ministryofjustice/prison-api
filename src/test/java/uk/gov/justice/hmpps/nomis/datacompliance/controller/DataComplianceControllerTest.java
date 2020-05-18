package uk.gov.justice.hmpps.nomis.datacompliance.controller;

import net.syscon.elite.api.model.PendingDeletionRequest;
import net.syscon.elite.api.resource.impl.ResourceTest;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.OffenderWithBookings;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.syscon.elite.executablespecification.steps.AuthTokenHelper.AuthToken.ELITE2_API_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.http.HttpStatus.ACCEPTED_202;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpMethod.POST;

public class DataComplianceControllerTest extends ResourceTest {

    private static final long BATCH_ID = 123L;

    // This date is 7 years after the SED_CALCULATED_DATE of the expected record
    private static final LocalDateTime WINDOW_START = LocalDateTime.of(2027, 3, 24, 0, 0);
    private static final LocalDateTime WINDOW_END = WINDOW_START;

    @SpyBean
    private DataComplianceEventPusher dataComplianceEventPusher;

    @Test
    public void requestOffenderPendingDeletions() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(ELITE2_API_USER),
                PendingDeletionRequest.builder()
                        .batchId(BATCH_ID)
                        .dueForDeletionWindowStart(WINDOW_START)
                        .dueForDeletionWindowEnd(WINDOW_END)
                        .build());

        final var response = testRestTemplate.exchange("/api/data-compliance/offenders/pending-deletions", POST, requestEntity, Void.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(ACCEPTED_202);

        verify(dataComplianceEventPusher, timeout(5000)).send(
                OffenderPendingDeletion.builder()
                        .offenderIdDisplay("Z0020ZZ")
                        .batchId(BATCH_ID)
                        .firstName("BURT")
                        .lastName("REYNOLDS")
                        .birthDate(LocalDate.of(1966, 1, 1))
                        .offender(OffenderWithBookings.builder()
                                .offenderId(-1020L)
                                .booking(new Booking(-20L))
                                .build())
                        .build());

        verify(dataComplianceEventPusher, timeout(5000))
                .send(new OffenderPendingDeletionReferralComplete(BATCH_ID));
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
