package uk.gov.justice.hmpps.nomis.datacompliance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent.OffenderWithBookings;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionReferralCompleteEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.OffenderDeletionEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderBookingPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderPendingDeletionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataComplianceReferralServiceTest {

    private static final long BATCH_ID = 123L;
    private static final LocalDateTime WINDOW_START = LocalDateTime.now();
    private static final LocalDateTime WINDOW_END = WINDOW_START.plusDays(1);

    private static final String OFFENDER_NUMBER_1 = "A1234AA";
    private static final String OFFENDER_NUMBER_2 = "B4321BB";

    @Mock
    private OffenderPendingDeletionRepository offenderPendingDeletionRepository;

    @Mock
    private OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;

    @Mock
    private OffenderDeletionEventPusher eventPusher;

    private DataComplianceReferralService service;

    @BeforeEach
    public void setUp() {
        service = new DataComplianceReferralService(
                offenderPendingDeletionRepository,
                offenderAliasPendingDeletionRepository,
                eventPusher);
    }

    @Test
    public void acceptOffendersPendingDeletion() throws Exception {

        when(offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(WINDOW_START.toLocalDate(), WINDOW_END.toLocalDate()))
                .thenReturn(List.of(
                        new OffenderPendingDeletion(OFFENDER_NUMBER_1),
                        new OffenderPendingDeletion(OFFENDER_NUMBER_2)));

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_1))
                .thenReturn(List.of(offenderAliasPendingDeletion(1)));
        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_2))
                .thenReturn(List.of(offenderAliasPendingDeletion(2)));

        service.acceptOffendersPendingDeletionRequest(BATCH_ID, WINDOW_START, WINDOW_END).get();

        verify(eventPusher).sendPendingDeletionEvent(expectedPendingDeletionEvent(1L, OFFENDER_NUMBER_1));
        verify(eventPusher).sendPendingDeletionEvent(expectedPendingDeletionEvent(2L, OFFENDER_NUMBER_2));
        verify(eventPusher).sendReferralCompleteEvent(expectedReferralCompleteEvent(BATCH_ID));
        verifyNoMoreInteractions(eventPusher);
    }

    @Test
    public void acceptOffendersPendingDeletionThrowsIfOffenderAliasesNotFound() {

        when(offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(WINDOW_START.toLocalDate(), WINDOW_END.toLocalDate()))
                .thenReturn(List.of(new OffenderPendingDeletion(OFFENDER_NUMBER_1)));

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_1))
                .thenReturn(emptyList());

        assertThatThrownBy(() -> service.acceptOffendersPendingDeletionRequest(BATCH_ID, WINDOW_START, WINDOW_END).get())
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Offender not found: 'A1234AA'");
    }

    @Test
    public void acceptOffendersPendingDeletionThrowsIfNoRootOffenderAliasFound() {

        when(offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(WINDOW_START.toLocalDate(), WINDOW_END.toLocalDate()))
                .thenReturn(List.of(new OffenderPendingDeletion(OFFENDER_NUMBER_1)));

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_1))
                .thenReturn(List.of(OffenderAliasPendingDeletion.builder()
                        .offenderId(1L)
                        .rootOffenderId(2L)
                        .build()));

        assertThatThrownBy(() -> service.acceptOffendersPendingDeletionRequest(BATCH_ID, WINDOW_START, WINDOW_END).get())
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot find root offender alias for 'A1234AA'");
    }

    private OffenderPendingDeletionEvent expectedPendingDeletionEvent(final long offenderId, final String offenderNumber) {
        return OffenderPendingDeletionEvent.builder()
                .offenderIdDisplay(offenderNumber)
                .batchId(123L)
                .firstName("John" + offenderId)
                .middleName("Middle" + offenderId)
                .lastName("Smith" + offenderId)
                .birthDate(LocalDate.of(2020, 1, (int) offenderId))
                .offender(OffenderWithBookings.builder()
                        .offenderId(offenderId)
                        .booking(new Booking(offenderId))
                        .build())
                .build();
    }

    private OffenderPendingDeletionReferralCompleteEvent expectedReferralCompleteEvent(final Long batchId) {
        return new OffenderPendingDeletionReferralCompleteEvent(batchId);
    }

    private OffenderAliasPendingDeletion offenderAliasPendingDeletion(final long offenderId) {
        return OffenderAliasPendingDeletion.builder()
                .firstName("John" + offenderId)
                .middleName("Middle" + offenderId)
                .lastName("Smith" + offenderId)
                .birthDate(LocalDate.of(2020, 1, (int) offenderId))
                .offenderId(offenderId)
                .rootOffenderId(offenderId)
                .offenderBooking(OffenderBookingPendingDeletion.builder()
                        .bookingId(offenderId)
                        .build())
                .build();
    }
}
