package net.syscon.elite.service;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.core.HasWriteScope;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.repository.jpa.model.OffenderIndividualSchedule;
import net.syscon.elite.repository.jpa.repository.AgencyLocationRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class PrisonToPrisonMoveSchedulingService {

    private final Clock clock;

    private final OffenderBookingRepository offenderBookingRepository;

    private final AgencyLocationRepository agencyLocationRepository;

    public PrisonToPrisonMoveSchedulingService(final Clock clock,
                                               final OffenderBookingRepository offenderBookingRepository,
                                               final AgencyLocationRepository agencyLocationRepository) {
        this.clock = clock;
        this.offenderBookingRepository = offenderBookingRepository;
        this.agencyLocationRepository = agencyLocationRepository;
    }

    @Transactional
    @VerifyBookingAccess
    @HasWriteScope
    public Object schedule(final Long bookingId, final String fromPrison, final String toPrison, final LocalDateTime moveDateTime) {
        checkIsInFuture(moveDateTime);
        checkNotTheSame(fromPrison, toPrison);

        final var activeBooking = activeOffenderBookingFor(bookingId);

        checkFromLocationMatchesThe(activeBooking, fromPrison);

        var scheduledMove = scheduleMove(activeBooking.getLocation(), getActive(toPrison), moveDateTime);

        log.debug("Prison to prison move scheduled with event id: {} for offender: {}, from: {}, to: {} on: {}",
                scheduledMove.getId(), activeBooking.getOffender().getNomsId(), fromPrison, toPrison, moveDateTime);

        return null;
    }

    private void checkIsInFuture(final LocalDateTime datetime) {
        checkArgument(datetime.isAfter(LocalDateTime.now(clock)), "Prison to prison move must be in the future.");
    }

    private OffenderBooking activeOffenderBookingFor(final Long bookingId) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));

        checkArgument(offenderBooking.isActive(), "Offender booking for prison to prison move with id %s is not active.", bookingId);

        return offenderBooking;
    }

    private void checkFromLocationMatchesThe(final OffenderBooking booking, final String from) {
        checkArgument(booking.getLocation().getId().equals(from), "Prison to prison move from prison does not match that of the booking.");
    }

    private void checkNotTheSame(final String from, final String to) {
        checkArgument(!from.equals(to), "Prison to prison move from and to prisons cannot be the same.");
    }

    private AgencyLocation getActive(final String prison) {
        final var agency = agencyLocationRepository.findById(prison).orElseThrow(() -> EntityNotFoundException.withMessage("Prison with id %s not found.", prison));

        checkArgument(agency.getActiveFlag().isActive(), "Prison with id %s not active.", prison);

        checkArgument(agency.getType().equals("INST"), "Prison to prison move to prison is not a prison.");

        return agency;
    }

    private OffenderIndividualSchedule scheduleMove(AgencyLocation fromPrison, AgencyLocation toPrison, LocalDateTime moveDateTime) {
        throw new UnsupportedOperationException("DT-780 not yet implemented.");
    }
}
