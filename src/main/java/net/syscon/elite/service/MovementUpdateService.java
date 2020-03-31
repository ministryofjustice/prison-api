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

    private final ReferenceDomainService referenceDomainService;
    private final BedAssignmentHistoryService bedAssignmentHistoryService;
    private final BookingService bookingService;

    public MovementUpdateService(ReferenceDomainService referenceDomainService, BedAssignmentHistoryService bedAssignmentHistoryService, BookingService bookingService) {
        this.referenceDomainService = referenceDomainService;
        this.bedAssignmentHistoryService = bedAssignmentHistoryService;
        this.bookingService = bookingService;
    }

    // @VerifyBookingAccess TODO DT-235 put this back - make sure it has a test dedicated to it
    public OffenderSummary moveToCell(final Long bookingId, final Long livingUnitId, final String reasonCode, final LocalDateTime dateTime) {
        referenceDomainService.getReferenceCodeByDomainAndCode(CELL_MOVE_REASON.getDomain(), reasonCode, false);
        final var offenderSummary = getOffenderSummary(bookingId);

        if (offenderSummary.getInternalLocationId().equals(String.valueOf(livingUnitId))) {
            return offenderSummary;
        }

        bookingService.updateLivingUnit(bookingId, livingUnitId);
        bedAssignmentHistoryService.add(bookingId, livingUnitId, reasonCode, dateTime);
        return getOffenderSummary(bookingId);
    }

    private OffenderSummary getOffenderSummary(final Long bookingId) {
        final var offenderSummary = bookingService.getLatestBookingByBookingId(bookingId);
        if (offenderSummary == null) {
            throw new EntityNotFoundException(format("Offender summary for booking id %d not found", bookingId));
        }
        return offenderSummary;
    }

}
