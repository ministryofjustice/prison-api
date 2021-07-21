package uk.gov.justice.hmpps.prison.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.RequestToRecall;
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner;
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.time.LocalDateTime;

import static java.lang.String.format;

@Service
public class SmokeTestHelperService {

    private final BookingService bookingService;
    private final PrisonerReleaseAndTransferService prisonerReleaseAndTransferService;
    private final OffenderBookingRepository offenderBookingRepository;

    public SmokeTestHelperService(BookingService bookingService, OffenderBookingRepository offenderBookingRepository,
                                  PrisonerReleaseAndTransferService prisonerReleaseAndTransferService) {
        this.bookingService = bookingService;
        this.offenderBookingRepository = offenderBookingRepository;
        this.prisonerReleaseAndTransferService = prisonerReleaseAndTransferService;
    }

    @Transactional
    @VerifyBookingAccess(overrideRoles = "SMOKE_TEST")
    @PreAuthorize("hasRole('SMOKE_TEST') and hasAuthority('SCOPE_write')")
    public void imprisonmentDataSetup(String offenderNo) {
        final var latestOffenderBooking = bookingService.getOffenderIdentifiers(offenderNo, "SMOKE_TEST");
        final var bookingAndSeq = getBookingAndSeqOrThrow(offenderNo, latestOffenderBooking);
        final var booking = offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, bookingAndSeq.getBookingSeq())
            .orElseThrow(EntityNotFoundException.withMessage(format("No booking found for offender %s and seq %d", offenderNo, bookingAndSeq.getBookingSeq())));

        final var currentImprisonmentStatus = booking.getActiveImprisonmentStatus().orElseThrow(() -> new BadRequestException(format("Unable to find active imprisonment status for offender number %s", offenderNo)));

        booking.setImprisonmentStatus(currentImprisonmentStatus.toBuilder().build(), LocalDateTime.now());
    }

    private OffenderBookingIdSeq.BookingAndSeq getBookingAndSeqOrThrow(String offenderNo, OffenderBookingIdSeq latestOffenderBooking) {
        return latestOffenderBooking.getBookingAndSeq()
            .orElseThrow(() -> EntityNotFoundException.withMessage(format("No booking found for offender %s", offenderNo)));
    }

    @Transactional
    @VerifyBookingAccess(overrideRoles = "SMOKE_TEST")
    @PreAuthorize("hasRole('SMOKE_TEST') and hasAuthority('SCOPE_write')")
    public void releasePrisoner(String offenderNo) {
        RequestToReleasePrisoner requestToReleasePrisoner = RequestToReleasePrisoner.builder()
            .commentText("Prisoner was released as part of smoke test")
            .movementReasonCode("CR")
            .build();
        prisonerReleaseAndTransferService.releasePrisoner(offenderNo, requestToReleasePrisoner, null);
    }

    @Transactional
    @VerifyBookingAccess(overrideRoles = "SMOKE_TEST")
    @PreAuthorize("hasRole('SMOKE_TEST') and hasAuthority('SCOPE_write')")
    public void recallPrisoner(String offenderNo) {
        RequestToRecall requestToRecall = RequestToRecall.builder()
            .prisonId("MDI")
            .movementReasonCode("24")
            .imprisonmentStatus("CUR_ORA")
            .cellLocation("MDI-RECP")
            .youthOffender(false)
            .build();
        prisonerReleaseAndTransferService.recallPrisoner(offenderNo, requestToRecall);
    }
}
