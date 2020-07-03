package net.syscon.prison.service;

import com.amazonaws.util.StringUtils;
import net.syscon.prison.api.model.OffenderBooking;
import net.syscon.prison.core.HasWriteScope;
import net.syscon.prison.repository.jpa.model.AgencyInternalLocation;
import net.syscon.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import net.syscon.prison.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.prison.security.VerifyBookingAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static net.syscon.prison.service.support.ReferenceDomain.CELL_MOVE_REASON;

@Service
@Transactional
public class MovementUpdateService {

    private final ReferenceDomainService referenceDomainService;
    private final BedAssignmentHistoryService bedAssignmentHistoryService;
    private final BookingService bookingService;
    private final OffenderBookingRepository offenderBookingRepository;
    private final AgencyInternalLocationRepository agencyInternalLocationRepository;
    private final Clock clock;

    public MovementUpdateService(
            final ReferenceDomainService referenceDomainService,
            final BedAssignmentHistoryService bedAssignmentHistoryService,
            final BookingService bookingService,
            final OffenderBookingRepository offenderBookingRepository,
            final AgencyInternalLocationRepository agencyInternalLocationRepository,
            final Clock clock) {
        this.referenceDomainService = referenceDomainService;
        this.bedAssignmentHistoryService = bedAssignmentHistoryService;
        this.bookingService = bookingService;
        this.offenderBookingRepository = offenderBookingRepository;
        this.agencyInternalLocationRepository = agencyInternalLocationRepository;
        this.clock = clock;
    }

    @Transactional
    @VerifyBookingAccess
    @HasWriteScope
    public OffenderBooking moveToCell(final Long bookingId, final String internalLocationDescription, final String reasonCode, final LocalDateTime dateTime) {
        validateMoveToCell(reasonCode, dateTime);
        final var movementDateTime = dateTime != null ? dateTime : LocalDateTime.now(clock);
        final var offenderBooking = getActiveOffenderBooking(bookingId);
        final var internalLocation = getActiveInternalLocation(internalLocationDescription);

        if (offenderBooking.getAssignedLivingUnitId().equals(internalLocation.getLocationId())) {
            return offenderBooking;
        }

        bookingService.updateLivingUnit(bookingId, internalLocationDescription);
        bedAssignmentHistoryService.add(bookingId, internalLocation.getLocationId(), reasonCode, movementDateTime);
        return getActiveOffenderBooking(bookingId);
    }

    private void validateMoveToCell(final String reasonCode, final LocalDateTime dateTime) {
        checkReasonCode(reasonCode);
        checkArgument(!StringUtils.isNullOrEmpty(reasonCode), "Reason code is mandatory");
        checkArgument(
                dateTime == null || dateTime.isBefore(LocalDateTime.now(clock)) || dateTime.isEqual(LocalDateTime.now(clock)),
                "The date cannot be in the future"
        );
    }

    private void checkReasonCode(String reasonCode) {
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(CELL_MOVE_REASON.getDomain(), reasonCode, false);
        } catch(EntityNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private OffenderBooking getActiveOffenderBooking(final Long bookingId) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(format("Booking id %d not found", bookingId)));
        checkArgument(offenderBooking.isActive(), "Offender booking with id %s is not active.", bookingId);
        return OffenderBooking.builder()
                .bookingId(offenderBooking.getBookingId())
                .agencyId(offenderBooking.getLocation().getId())
                .assignedLivingUnitId(offenderBooking.getAssignedLivingUnit().getLocationId())
                .assignedLivingUnitDesc(offenderBooking.getAssignedLivingUnit().getDescription())
                .build();
    }

    private AgencyInternalLocation getActiveInternalLocation(final String locationDescription) {
        final var internalLocation = agencyInternalLocationRepository.findOneByDescription(locationDescription)
                .orElseThrow(EntityNotFoundException.withMessage(format("Location description %s not found", locationDescription)));
        checkArgument(internalLocation.isActive(), "Location %s is not active", locationDescription);
        return internalLocation;
    }


}
