package uk.gov.justice.hmpps.prison.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImprisonmentStatusRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.time.LocalDateTime;

import static java.lang.String.format;

@Service
public class SmokeTestHelperService {

    private final BookingService bookingService;
    private final OffenderImprisonmentStatusRepository imprisonmentStatusRepository;

    public SmokeTestHelperService(BookingService bookingService, OffenderImprisonmentStatusRepository imprisonmentStatusRepository) {
        this.bookingService = bookingService;
        this.imprisonmentStatusRepository = imprisonmentStatusRepository;
    }

    @Transactional
    @VerifyBookingAccess(overrideRoles = "SMOKE_TEST")
    @HasWriteScope
    @PreAuthorize("hasRole('SMOKE_TEST')")
    public void imprisonmentDataSetup(String offenderNo) {
        final var latestOffenderBooking = bookingService.getOffenderIdentifiers(offenderNo, "SMOKE_TEST");
        final var bookingAndSeq = latestOffenderBooking.getBookingAndSeq()
                .orElseThrow(() -> EntityNotFoundException.withMessage("No booking found for offender %s", offenderNo));
        final var latestStatus = imprisonmentStatusRepository.findByOffenderBookId(bookingAndSeq.getBookingId())
                .stream()
                .filter(status -> status.getLatestStatus().equals("Y"))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(format("Unable to find active imprisonment status for offender number %s", offenderNo)));

        final var now = LocalDateTime.now();
        final var newStatus = latestStatus.toBuilder()
                .imprisonStatusSeq(latestStatus.getImprisonStatusSeq() + 1)
                .effectiveDate(now.toLocalDate())
                .effectiveTime(now)
                .build();
        imprisonmentStatusRepository.save(newStatus);

        latestStatus.setLatestStatus("N");
        latestStatus.setExpiryDate(now);
    }
}
