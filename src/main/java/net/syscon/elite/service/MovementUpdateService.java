package net.syscon.elite.service;

import com.amazonaws.util.StringUtils;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.core.HasWriteScope;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static net.syscon.elite.service.support.ReferenceDomain.CELL_MOVE_REASON;

@Service
@Transactional
public class MovementUpdateService {

    private final ReferenceDomainService referenceDomainService;
    private final BedAssignmentHistoryService bedAssignmentHistoryService;
    private final BookingService bookingService;
    private final OffenderBookingRepository offenderBookingRepository;
    private final Clock clock;

    public MovementUpdateService(
            final ReferenceDomainService referenceDomainService,
            final BedAssignmentHistoryService bedAssignmentHistoryService,
            final BookingService bookingService,
            final OffenderBookingRepository offenderBookingRepository,
            final Clock clock) {
        this.referenceDomainService = referenceDomainService;
        this.bedAssignmentHistoryService = bedAssignmentHistoryService;
        this.bookingService = bookingService;
        this.offenderBookingRepository = offenderBookingRepository;
        this.clock = clock;
    }

    @Transactional
    @VerifyBookingAccess
    @HasWriteScope
    public OffenderBooking moveToCell(final Long bookingId, final Long livingUnitId, final String reasonCode, final LocalDateTime dateTime) {
        validateMoveToCell(reasonCode, dateTime);
        final var movementDateTime = dateTime != null ? dateTime : LocalDateTime.now(clock);
        referenceDomainService.getReferenceCodeByDomainAndCode(CELL_MOVE_REASON.getDomain(), reasonCode, false);
        final var offenderBooking = getActiveOffenderBooking(bookingId);

        if (offenderBooking.getAssignedLivingUnitId().equals(livingUnitId)) {
            return offenderBooking;
        }

        bookingService.updateLivingUnit(bookingId, livingUnitId);
        bedAssignmentHistoryService.add(bookingId, livingUnitId, reasonCode, movementDateTime);
        return getActiveOffenderBooking(bookingId);
    }

    private void validateMoveToCell(final String reasonCode, final LocalDateTime dateTime) {
        checkArgument(!StringUtils.isNullOrEmpty(reasonCode), "Reason code is mandatory");
        checkArgument(
                dateTime == null || dateTime.isBefore(LocalDateTime.now()) || dateTime.isEqual(LocalDateTime.now()),
                "The date cannot be in the future"
        );
    }

    private OffenderBooking getActiveOffenderBooking(final Long bookingId) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withMessage(format("Booking id %d not found", bookingId)));
        checkArgument(offenderBooking.isActive(), "Offender booking with id %s is not active.", bookingId);
        return OffenderBooking.builder()
                .bookingId(offenderBooking.getBookingId())
                .agencyId(offenderBooking.getLocation().getId())
                .assignedLivingUnitId(offenderBooking.getAssignedLivingUnitId())
                .build();
    }

}
