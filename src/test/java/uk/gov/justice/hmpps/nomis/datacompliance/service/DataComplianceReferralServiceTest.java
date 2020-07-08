package uk.gov.justice.hmpps.nomis.datacompliance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.OffenderAlias;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderBookingPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderChargePendingDeletion;
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
    private static final PageRequest PAGE_REQUEST = PageRequest.of(0, 2);
    private static final long TOTAL_IN_WINDOW = 10;

    private static final String OFFENDER_NUMBER_1 = "A1234AA";
    private static final String OFFENDER_NUMBER_2 = "B4321BB";

    @Mock
    private OffenderPendingDeletionRepository offenderPendingDeletionRepository;

    @Mock
    private OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;

    @Mock
    private DataComplianceEventPusher eventPusher;

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
                .getOffendersDueForDeletionBetween(WINDOW_START.toLocalDate(), WINDOW_END.toLocalDate(), PAGE_REQUEST))
                .thenReturn(new PageImpl<>(List.of(
                        new uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion(OFFENDER_NUMBER_1),
                        new uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion(OFFENDER_NUMBER_2)),
                        PAGE_REQUEST, TOTAL_IN_WINDOW));

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_1))
                .thenReturn(List.of(offenderAliasPendingDeletion(1)));
        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_2))
                .thenReturn(List.of(offenderAliasPendingDeletion(2)));

        service.acceptOffendersPendingDeletionRequest(BATCH_ID, WINDOW_START, WINDOW_END, PAGE_REQUEST).get();

        verify(eventPusher).send(expectedPendingDeletionEvent(1L, OFFENDER_NUMBER_1));
        verify(eventPusher).send(expectedPendingDeletionEvent(2L, OFFENDER_NUMBER_2));
        verify(eventPusher).send(expectedReferralCompleteEvent(BATCH_ID, 2L, TOTAL_IN_WINDOW));
        verifyNoMoreInteractions(eventPusher);
    }

    @Test
    public void acceptOffendersPendingDeletionThrowsIfOffenderAliasesNotFound() {

        when(offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(WINDOW_START.toLocalDate(), WINDOW_END.toLocalDate(), PAGE_REQUEST))
                .thenReturn(new PageImpl<>(List.of(
                        new uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion(OFFENDER_NUMBER_1))));

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_1))
                .thenReturn(emptyList());

        assertThatThrownBy(() -> service.acceptOffendersPendingDeletionRequest(BATCH_ID, WINDOW_START, WINDOW_END, PAGE_REQUEST).get())
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Offender not found: 'A1234AA'");
    }

    @Test
    public void acceptOffendersPendingDeletionThrowsIfNoRootOffenderAliasFound() {

        when(offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(WINDOW_START.toLocalDate(), WINDOW_END.toLocalDate(), PAGE_REQUEST))
                .thenReturn(new PageImpl<>(List.of(
                        new uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion(OFFENDER_NUMBER_1))));

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_1))
                .thenReturn(List.of(OffenderAliasPendingDeletion.builder()
                        .offenderId(1L)
                        .rootOffenderId(2L)
                        .build()));

        assertThatThrownBy(() -> service.acceptOffendersPendingDeletionRequest(BATCH_ID, WINDOW_START, WINDOW_END, PAGE_REQUEST).get())
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot find root offender alias for 'A1234AA'");
    }

    private OffenderPendingDeletion expectedPendingDeletionEvent(final long offenderId, final String offenderNumber) {
        return OffenderPendingDeletion.builder()
                .offenderIdDisplay(offenderNumber)
                .batchId(123L)
                .firstName("John" + offenderId)
                .middleName("Middle" + offenderId)
                .lastName("Smith" + offenderId)
                .birthDate(LocalDate.of(2020, 1, (int) offenderId))
                .offenderAlias(OffenderAlias.builder()
                        .offenderId(offenderId)
                        .booking(Booking.builder()
                                .offenderBookId(offenderId)
                                .offenceCode("offence" + offenderId)
                                .build())
                        .build())
                .build();
    }

    private OffenderPendingDeletionReferralComplete expectedReferralCompleteEvent(final Long batchId,
                                                                                  final Long numberReferred,
                                                                                  final Long totalInWindow) {
        return new OffenderPendingDeletionReferralComplete(batchId, numberReferred, totalInWindow);
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
                        .offenderCharge(OffenderChargePendingDeletion.builder().offenceCode("offence" + offenderId).build())
                        .build())
                .build();
    }
}
