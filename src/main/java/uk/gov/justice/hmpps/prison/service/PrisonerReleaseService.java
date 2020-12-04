package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementTypeAndReason.Pk;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MovementTypeAndReasonRespository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderKeyDateAdjustmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import java.time.LocalDateTime;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.REL;

@Service
@Transactional
@Validated
@Slf4j
@AllArgsConstructor
public class PrisonerReleaseService {

    private final OffenderBookingRepository offenderBookingRepository;
    private final AgencyLocationRepository agencyLocationRepository;
    private final ExternalMovementRepository externalMovementRepository;
    private final AgencyService agencyService;

    private final ReferenceCodeRepository<MovementType> movementTypeRepository;
    private final ReferenceCodeRepository<MovementReason> movementReasonRepository;
    private final BedAssignmentHistoriesRepository bedAssignmentHistoriesRepository;
    private final AgencyInternalLocationRepository agencyInternalLocationRepository;
    private final MovementTypeAndReasonRespository movementTypeAndReasonRespository;
    private final OffenderSentenceAdjustmentRepository offenderSentenceAdjustmentRepository;
    private final OffenderKeyDateAdjustmentRepository offenderKeyDateAdjustmentRepository;

    public void releasePrisoner(final String prisonerIdentifier, final String movementReasonCode, final String commentText) {

        // check that prisoner is active in
        final var optionalOffenderBooking = offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(prisonerIdentifier, 1);

        final var booking = optionalOffenderBooking.orElseThrow(EntityNotFoundException.withMessage("No bookings found for prisoner number %s", prisonerIdentifier));

        if (!booking.getActiveFlag().equals("Y")) {
            throw new BadRequestException("Prisoner is not currently active");
        }

        if (!booking.getInOutStatus().equals("IN")) {
            throw new BadRequestException("Prisoner is not currently IN");
        }

        // unless you are system user you must have the caseload
        if (!AuthenticationFacade.hasRoles("SYSTEM_USER")) {
            agencyService.verifyAgencyAccess(booking.getLocation().getId());
        }

        final var releaseMovementTypeAndReason = Pk.builder().type(REL.getCode()).reasonCode(movementReasonCode).build();
        movementTypeAndReasonRespository.findById(releaseMovementTypeAndReason)
            .orElseThrow(EntityNotFoundException.withMessage("No movement type found for {}", releaseMovementTypeAndReason));

        // set previous active movements to false
        final var bookingId = booking.getBookingId();
        externalMovementRepository.findAllByBookingIdAndActiveFlag(bookingId, ActiveFlag.Y)
            .forEach(m -> m.setActiveFlag(ActiveFlag.N));

        final var outLocation = agencyLocationRepository.findById(AgencyLocation.OUT).orElseThrow(EntityNotFoundException.withMessage("No %s agency found", AgencyLocation.OUT));

        // Generate the external movement out
        final var releaseDateTime = LocalDateTime.now();
        final var releaseMovement = ExternalMovement.builder()
            .bookingId(bookingId)
            .movementSequence(externalMovementRepository.getLatestMovementSequence(bookingId) + 1)
            .movementDate(releaseDateTime.toLocalDate())
            .movementTime(releaseDateTime)
            .movementType(movementTypeRepository.findById(REL).orElseThrow(EntityNotFoundException.withMessage("No %s movement type found", REL.getCode())))
            .movementReason(movementReasonRepository.findById(MovementReason.pk(movementReasonCode)).orElseThrow(EntityNotFoundException.withMessage("No movement reason %s found", movementReasonCode)))
            .movementDirection(MovementDirection.OUT)
            .fromAgency(booking.getLocation())
            .toAgency(outLocation)
            .activeFlag(ActiveFlag.Y)
            .commentText(commentText)
            .build();
        externalMovementRepository.save(releaseMovement);

        // Update occupancy (recursively)
        incrementCurrentOccupancy(booking.getAssignedLivingUnit());

        // Update Bed Assignment
        bedAssignmentHistoriesRepository.findByBedAssignmentHistoryPKOffenderBookingIdAndBedAssignmentHistoryPKSequence(bookingId,
            bedAssignmentHistoriesRepository.getMaxSeqForBookingId(bookingId)).ifPresent(b -> {
                if (b.getAssignmentEndDate() == null && b.getAssignmentEndDateTime() == null) {
                    b.setAssignmentEndDate(releaseDateTime.toLocalDate());
                    b.setAssignmentEndDateTime(releaseDateTime);
                }
        });

        offenderSentenceAdjustmentRepository.findAllByOffenderBookIdAndActiveFlag(bookingId, ActiveFlag.Y)
            .forEach(s -> s.setActiveFlag(ActiveFlag.N));

        offenderKeyDateAdjustmentRepository.findAllByOffenderBookIdAndActiveFlag(bookingId, ActiveFlag.Y)
            .forEach(s -> s.setActiveFlag(ActiveFlag.N));

        // update the booking record
        booking.setInOutStatus("OUT");
        booking.setActiveFlag("N");
        booking.setBookingStatus("C");
        booking.setLivingUnitMv(null);
        booking.setAssignedLivingUnit(null);
        booking.setLocation(outLocation);
        booking.setBookingEndDate(releaseDateTime);
        booking.setStatusReason(REL.getCode() + "-" + movementReasonCode);
        booking.setCommStatus(null);

    }

    private void incrementCurrentOccupancy(final AgencyInternalLocation assignedLivingUnit) {
        if (assignedLivingUnit != null) {
            assignedLivingUnit.incrementCurrentOccupancy();
            agencyInternalLocationRepository.save(assignedLivingUnit);
            incrementCurrentOccupancy(assignedLivingUnit.getParentLocation());
        }
    }
}
