package net.syscon.elite.service;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.core.HasWriteScope;
import net.syscon.elite.repository.jpa.repository.CourtEventRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class CourtHearingCancellationService {

    private final CourtEventRepository courtEventRepository;

    public CourtHearingCancellationService(final CourtEventRepository courtEventRepository) {
        this.courtEventRepository = courtEventRepository;
    }

    @Transactional
    @VerifyBookingAccess(overrideRoles = "COURT_HEARING_MAINTAINER")
    @HasWriteScope
    @PreAuthorize("hasRole('COURT_HEARING_MAINTAINER')")
    public void cancel(final Long bookingId, final Long hearingId) {
        final var courtHearing = courtEventRepository.findByOffenderBooking_BookingIdAndId(bookingId, hearingId)
                .orElseThrow(EntityNotFoundException.withMessage("Court hearing '%s' with booking '%s' not found.", hearingId, bookingId));

        courtEventRepository.delete(courtHearing);

        log.debug("Deleted court event '{}' for booking '{}'", hearingId, bookingId);
    }
}
