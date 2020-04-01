package net.syscon.elite.service;

import com.amazonaws.util.StringUtils;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.security.VerifyBookingAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static net.syscon.elite.service.support.ReferenceDomain.CELL_MOVE_REASON;

@Service
@Transactional
public class MovementUpdateService {

    private final ReferenceDomainService referenceDomainService;
    private final BedAssignmentHistoryService bedAssignmentHistoryService;
    private final BookingService bookingService;
    private final Clock clock;

    public MovementUpdateService(ReferenceDomainService referenceDomainService, BedAssignmentHistoryService bedAssignmentHistoryService, BookingService bookingService, Clock clock) {
        this.referenceDomainService = referenceDomainService;
        this.bedAssignmentHistoryService = bedAssignmentHistoryService;
        this.bookingService = bookingService;
        this.clock = clock;
    }

    @VerifyBookingAccess
    public OffenderSummary moveToCell(final Long bookingId, final Long livingUnitId, final String reasonCode, final LocalDateTime dateTime) {
        validateMoveToCellRequest(reasonCode, dateTime);
        final var movementDateTime = dateTime != null ? dateTime : LocalDateTime.now(clock);
        referenceDomainService.getReferenceCodeByDomainAndCode(CELL_MOVE_REASON.getDomain(), reasonCode, false);
        final var offenderSummary = getOffenderSummary(bookingId);

        if (offenderSummary.getInternalLocationId().equals(String.valueOf(livingUnitId))) {
            return offenderSummary;
        }

        bookingService.updateLivingUnit(bookingId, livingUnitId);
        bedAssignmentHistoryService.add(bookingId, livingUnitId, reasonCode, movementDateTime);
        return getOffenderSummary(bookingId);
    }

    private void validateMoveToCellRequest(final String reasonCode, LocalDateTime dateTime) {
        if (StringUtils.isNullOrEmpty(reasonCode)) {
            throw new IllegalArgumentException("Reason code is mandatory");
        }
        if (dateTime != null && dateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("The date cannot be in the future");
        }
    }

    private OffenderSummary getOffenderSummary(final Long bookingId) {
        final var offenderSummary = bookingService.getLatestBookingByBookingId(bookingId);
        if (offenderSummary == null) {
            throw new EntityNotFoundException(format("Offender summary for booking id %d not found", bookingId));
        }
        return offenderSummary;
    }

}
