package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.datacompliance.config.DataComplianceProperties;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderBookingPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDeletionService.OffenderDeletionGrant;
import uk.gov.justice.hmpps.prison.repository.OffenderDeletionRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderDeletionServiceTest {

    private static final String OFFENDER_NUMBER = "A1234AA";
    private static final Long REFERRAL_ID = 123L;
    private static final Long OFFENDER_ID = 456L;
    private static final Long OFFENDER_BOOK_ID = 789L;

    @Mock
    private OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;

    @Mock
    private OffenderDeletionRepository offenderDeletionRepository;

    @Mock
    private DataComplianceEventPusher dataComplianceEventPusher;

    @Mock
    private TelemetryClient telemetryClient;

    private OffenderDeletionService service;

    @BeforeEach
    public void setUp() {

        service = new OffenderDeletionService(
                new DataComplianceProperties(true),
                offenderAliasPendingDeletionRepository,
                offenderDeletionRepository,
                dataComplianceEventPusher,
                telemetryClient);
    }

    @Test
    public void deleteOffender() {

        mockOffenderIds();

        when(offenderDeletionRepository.cleanseOffenderData(OFFENDER_NUMBER)).thenReturn(Set.of(OFFENDER_ID));

        service.deleteOffender(OffenderDeletionGrant.builder()
                .offenderNo(OFFENDER_NUMBER)
                .referralId(REFERRAL_ID)
                .offenderId(OFFENDER_ID)
                .offenderBookId(OFFENDER_BOOK_ID)
                .build());

        verify(dataComplianceEventPusher).send(new OffenderDeletionComplete(OFFENDER_NUMBER, REFERRAL_ID));
        verify(telemetryClient).trackEvent("OffenderDelete", Map.of("offenderNo", OFFENDER_NUMBER, "count", "1"), null);
    }

    @Test
    void deleteOffenderThrowsIfOffenderIdsDoNotMatch() {

        mockOffenderIds();

        assertThatThrownBy(() -> service.deleteOffender(OffenderDeletionGrant.builder()
                .offenderNo(OFFENDER_NUMBER)
                .referralId(REFERRAL_ID)
                .offenderId(999L)
                .offenderBookId(OFFENDER_BOOK_ID)
                .build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The requested offender ids ([999]) do not match those currently linked to offender 'A1234AA' ([456])");

        verifyNoInteractions(offenderDeletionRepository);
    }

    @Test
    void deleteOffenderThrowsIfOffenderBookIdsDoNotMatch() {

        mockOffenderIds();

        assertThatThrownBy(() -> service.deleteOffender(OffenderDeletionGrant.builder()
                .offenderNo(OFFENDER_NUMBER)
                .referralId(REFERRAL_ID)
                .offenderId(OFFENDER_ID)
                .offenderBookId(999L)
                .build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The requested offender book ids ([999]) do not match those currently linked to offender 'A1234AA' ([789])");

        verifyNoInteractions(offenderDeletionRepository);
    }

    @Test
    void deleteOffenderThrowsIfDeletionNotEnabled() {

        service = new OffenderDeletionService(
                new DataComplianceProperties(false),
                offenderAliasPendingDeletionRepository,
                offenderDeletionRepository,
                dataComplianceEventPusher,
                telemetryClient);

        assertThatThrownBy(() -> service.deleteOffender(OffenderDeletionGrant.builder()
                .offenderNo(OFFENDER_NUMBER)
                .referralId(REFERRAL_ID)
                .offenderId(OFFENDER_ID)
                .offenderBookId(OFFENDER_BOOK_ID)
                .build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Deletion is not enabled!");
    }

    private void mockOffenderIds() {
        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER))
                .thenReturn(List.of(OffenderAliasPendingDeletion.builder()
                        .offenderId(OFFENDER_ID)
                        .offenderBooking(OffenderBookingPendingDeletion.builder().bookingId(OFFENDER_BOOK_ID).build())
                        .build()));
    }
}
