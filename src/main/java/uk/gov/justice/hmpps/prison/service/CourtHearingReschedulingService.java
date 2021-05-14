package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.CourtHearing;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Service to support rescheduling of existing scheduled court hearings.  Only future scheduled court hearings are eligible to be rescheduled.
 */
@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class CourtHearingReschedulingService {

    private final CourtEventRepository courtEventRepository;

    private final Clock clock;

    public CourtHearingReschedulingService(final CourtEventRepository courtEventRepository, final Clock clock) {
        this.courtEventRepository = courtEventRepository;
        this.clock = clock;
    }

    @Transactional
    @VerifyBookingAccess(overrideRoles = "COURT_HEARING_MAINTAINER")
    @PreAuthorize("hasRole('COURT_HEARING_MAINTAINER') and hasAuthority('SCOPE_write')")
    public CourtHearing reschedule(final Long bookingId, final Long hearingId, final LocalDateTime revisedDateTime) {
        final var scheduledCourtHearing = getScheduledHearingFor(hearingId);

        checkBookingsMatch(bookingId, scheduledCourtHearing.getOffenderBooking());
        checkRevisedDateTimeIsInFuture(revisedDateTime);

        if (nothingToChange(revisedDateTime, scheduledCourtHearing)) {
            return transformToCourtHearing(scheduledCourtHearing);
        }

        final var originalHearingDateTime = scheduledCourtHearing.getEventDateTime();

        scheduledCourtHearing.setEventDateTime(revisedDateTime);

        final var rescheduledCourtHearing = courtEventRepository.save(scheduledCourtHearing);

        log.debug("Rescheduled court hearing '{}' for offender '{}' from '{}' to '{}'",
                rescheduledCourtHearing.getId(),
                rescheduledCourtHearing.getOffenderBooking().getOffender().getNomsId(),
                originalHearingDateTime,
                revisedDateTime);

        return transformToCourtHearing(rescheduledCourtHearing);
    }

    private void checkBookingsMatch(final Long bookingId, OffenderBooking booking) {
        checkArgument(bookingId.equals(booking.getBookingId()), "Booking id '%s'does not match that on the hearing.", bookingId);
    }

    private void checkRevisedDateTimeIsInFuture(final LocalDateTime dateTime) {
        checkArgument(dateTime.isAfter(LocalDateTime.now(clock)), "Revised court hearing date '%s' must be in the future.", dateTime);
    }

    private boolean nothingToChange(final LocalDateTime newDateTime, final CourtEvent existing) {
        return newDateTime.equals(existing.getEventDateTime());
    }

    private CourtEvent getScheduledHearingFor(final Long hearingId) {
        final var hearing = courtEventRepository.findById(hearingId).orElseThrow(() -> EntityNotFoundException.withMessage("Court hearing with id '{}' not found.", hearingId));

        checkState(hearing.getEventStatus().isScheduled(), "The existing court hearing '%s' must be in a scheduled state to reschedule.", hearingId);
        checkState(hearing.getEventDateTime().isAfter(LocalDateTime.now(clock)), "The existing court hearing '%s' cannot be rescheduled as its start date is in the past.", hearingId);
        return hearing;
    }

    private CourtHearing transformToCourtHearing(final CourtEvent event) {
        return CourtHearing.builder()
                .id(event.getId())
                .location(AgencyTransformer.transform(event.getCourtLocation(), false))
                .dateTime(event.getEventDateTime())
                .build();
    }
}
