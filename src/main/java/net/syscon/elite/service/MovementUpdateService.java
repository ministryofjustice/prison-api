package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static net.syscon.elite.service.support.ReferenceDomain.CELL_MOVE_REASON;

@Service
@Transactional
public class MovementUpdateService {

    private ReferenceDomainService referenceDomainService;
    private LocationService locationService;
    private BookingService bookingService;

    public MovementUpdateService(ReferenceDomainService referenceDomainService, LocationService locationService, BookingService bookingService) {
        this.referenceDomainService = referenceDomainService;
        this.locationService = locationService;
        this.bookingService = bookingService;
    }

    // @VerifyBookingAccess TODO DT-235 put this back - make sure it has a test dedicated to it
    public OffenderSummary moveToCell(final Long bookingId, final Long livingUnitId, final String reasonCode, final LocalDateTime dateTime) {
        referenceDomainService.getReferenceCodeByDomainAndCode(CELL_MOVE_REASON.getDomain(), reasonCode, false);
        final var offenderSummary = getOffenderSummary(bookingId);
        final var location = locationService.getLocation(livingUnitId);

        if (!offenderSummary.getAgencyLocationId().equals(location.getAgencyId())) {
            throw new IllegalArgumentException(format("Move to living unit in prison %s invalid for offender in prison %s", location.getAgencyId(), offenderSummary.getAgencyLocationId()));
        }

        if (offenderSummary.getInternalLocationId().equals(String.valueOf(livingUnitId))) {
            return offenderSummary;
        }

        bookingService.updateLivingUnit(bookingId, livingUnitId);
        locationService.addBedAssignmentHistory(bookingId, livingUnitId);
        return getOffenderSummary(bookingId);
    }

    private OffenderSummary getOffenderSummary(Long bookingId) {
        final var offenderSummary = bookingService.getLatestBookingByBookingId(bookingId);
        if (offenderSummary == null) {
            throw new EntityNotFoundException(format("Offender summary for booking id %d not found", bookingId));
        }
        return offenderSummary;
    }

}
