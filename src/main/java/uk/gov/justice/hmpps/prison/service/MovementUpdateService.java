package uk.gov.justice.hmpps.prison.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.CellMoveResult;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static uk.gov.justice.hmpps.prison.service.support.ReferenceDomain.CELL_MOVE_REASON;

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
    public CellMoveResult moveToCell(final Long bookingId, final String internalLocationDescription, final String reasonCode, final LocalDateTime dateTime) {
        validateMoveToCell(reasonCode, dateTime);

        final var movementDateTime = dateTime != null ? dateTime : LocalDateTime.now(clock);
        final var offenderBooking = getActiveOffenderBooking(bookingId);
        final var internalLocation = getActiveInternalLocation(internalLocationDescription);

        if (offenderBooking.getAssignedLivingUnitId().equals(internalLocation.getLocationId()))
            return transformToCellSwapResult(offenderBooking);

        if (!internalLocation.isActiveCellWithSpace(false))
            throw new IllegalArgumentException(String.format("Location %s is either not a cell, active or is at maximum capacity", internalLocation.getDescription()));

        return saveAndReturnCellMoveResult(bookingId, reasonCode, movementDateTime, internalLocation);
    }

    @Transactional
    @VerifyBookingAccess
    @HasWriteScope
    public CellMoveResult moveToCellSwap(final Long bookingId, final String reasonCode, final LocalDateTime dateTime) {
        final var reason = reasonCode == null ? "ADM" : reasonCode;

        validateMoveToCell(reason, dateTime);

        final var movementDateTime = dateTime != null ? dateTime : LocalDateTime.now(clock);
        final var offenderBooking = getActiveOffenderBooking(bookingId);
        final var agency = offenderBooking.getAgencyId();
        final var internalLocation = getCswapLocation(agency);

        if (offenderBooking.getAssignedLivingUnitId().equals(internalLocation.getLocationId()))
            return transformToCellSwapResult(offenderBooking);

        return saveAndReturnCellMoveResult(bookingId, reason, movementDateTime, internalLocation);
    }

    private CellMoveResult saveAndReturnCellMoveResult(final long bookingId, final String reasonCode,
                                                       final LocalDateTime movementDateTime,
                                                       final AgencyInternalLocation location) {

        bookingService.updateLivingUnit(bookingId, location);

        final var bookingAndSequence =
                bedAssignmentHistoryService.add(bookingId, location.getLocationId(), reasonCode, movementDateTime);

        final var latestOffenderBooking = getActiveOffenderBooking(bookingId);

        return transformToCellSwapResult(latestOffenderBooking, bookingAndSequence.getSequence());
    }

    private void validateMoveToCell(final String reasonCode, final LocalDateTime dateTime) {
        checkReasonCode(reasonCode);
        checkArgument(!StringUtils.isEmpty(reasonCode), "Reason code is mandatory");
        checkDate(dateTime);
    }

    private void checkReasonCode(final String reasonCode) {
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(CELL_MOVE_REASON.getDomain(), reasonCode, false);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private void checkDate(final LocalDateTime dateTime) {
        checkArgument(
                dateTime == null || dateTime.isBefore(LocalDateTime.now(clock)) || dateTime.isEqual(LocalDateTime.now(clock)),
                "The date cannot be in the future"
        );
    }

    private CellMoveResult transformToCellSwapResult(final OffenderBooking offenderBooking) {
        return transformToCellSwapResult(offenderBooking, null);
    }

    private CellMoveResult transformToCellSwapResult(final OffenderBooking offenderBooking, final Integer bedAssignmentHistorySequence) {
        return CellMoveResult.builder()
                .bookingId(offenderBooking.getBookingId())
                .agencyId(offenderBooking.getAgencyId())
                .assignedLivingUnitId(offenderBooking.getAssignedLivingUnitId())
                .assignedLivingUnitDesc(offenderBooking.getAssignedLivingUnitDesc())
                .bedAssignmentHistorySequence(bedAssignmentHistorySequence)
                .build();
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

    private AgencyInternalLocation getCswapLocation(final String agency) {
        final var cellSwapLocations = agencyInternalLocationRepository
                .findByLocationCodeAndAgencyId("CSWAP", agency)
                .stream()
                .filter(AgencyInternalLocation::isCellSwap)
                .toList();

        if (cellSwapLocations.size() > 1)
            throw new RuntimeException("There are more than 1 CSWAP locations configured");

        return cellSwapLocations
                .stream()
                .findFirst()
                .orElseThrow(EntityNotFoundException.withMessage(format("CSWAP location not found for %s", agency)));
    }

    private AgencyInternalLocation getActiveInternalLocation(final String locationDescription) {
        final var internalLocation = agencyInternalLocationRepository.findOneByDescription(locationDescription)
                .orElseThrow(EntityNotFoundException.withMessage(format("Location description %s not found", locationDescription)));
        checkArgument(internalLocation.isActive(), "Location %s is not active", locationDescription);
        return internalLocation;
    }
}
