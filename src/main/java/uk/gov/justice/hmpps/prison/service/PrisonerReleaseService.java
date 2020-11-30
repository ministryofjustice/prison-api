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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;

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

    private final ReferenceCodeRepository<MovementType> movementTypeRepository;
    private final ReferenceCodeRepository<MovementReason> movementReasonRepository;
    private final BedAssignmentHistoriesRepository bedAssignmentHistoriesRepository;

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

        // set previous active movements to false
        final var bookingId = booking.getBookingId();
        externalMovementRepository.findAllByBookingIdAndActiveFlag(bookingId, ActiveFlag.Y)
            .forEach(m -> m.setActiveFlag(ActiveFlag.N));

        final var outLocation = agencyLocationRepository.findById(AgencyLocation.OUT).orElseThrow(EntityNotFoundException.withMessage("No %s agency found", AgencyLocation.OUT));

        // Generate the external movement out
        final var releaseDateTime = LocalDateTime.now();
        externalMovementRepository.save(ExternalMovement.builder()
            .booking(booking)
            .movementSequence(externalMovementRepository.getLatestMovementSequence(bookingId)+1)
            .movementTime(releaseDateTime)
            .movementType(movementTypeRepository.findById(REL).orElseThrow(EntityNotFoundException.withMessage("No %s movement type found", REL.getCode())))
            .movementReason(movementReasonRepository.findById(MovementReason.pk(movementReasonCode)).orElseThrow(EntityNotFoundException.withMessage("No movement reason %s found", movementReasonCode)))
            .movementDirection(MovementDirection.OUT)
            .fromAgency(booking.getLocation())
            .toAgency(outLocation)
            .activeFlag(ActiveFlag.Y)
            .commentText(commentText)
            .build());

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

        // Update Bed Assignment
        bedAssignmentHistoriesRepository.findByBedAssignmentHistoryPKOffenderBookingIdAndBedAssignmentHistoryPKSequence(bookingId,
            bedAssignmentHistoriesRepository.getMaxSeqForBookingId(bookingId)).ifPresent(b -> {
                if (b.getAssignmentEndDate() == null && b.getAssignmentEndDateTime() == null) {
                    b.setAssignmentEndDate(releaseDateTime.toLocalDate());
                    b.setAssignmentEndDateTime(releaseDateTime);
                }
        });


        // Update occupancy (recursively)
        incrementCurrentOccupancy(booking.getAssignedLivingUnit());

        /** UPDATE OFFENDER_SENTENCE_ADJUSTS SET ACTIVE_FLAG = 'N'
         WHERE OFFENDER_BOOK_ID = :B1 AND ACTIVE_FLAG = 'Y'

         UPDATE OFFENDER_KEY_DATE_ADJUSTS SET ACTIVE_FLAG = 'N'
         WHERE OFFENDER_BOOK_ID = :B1 AND ACTIVE_FLAG = 'Y' */
    }

    private void incrementCurrentOccupancy(final AgencyInternalLocation assignedLivingUnit) {
        if (assignedLivingUnit != null) {
            assignedLivingUnit.incrementCurrentOccupancy();
            incrementCurrentOccupancy(assignedLivingUnit.getParentLocation());
        }
    }
}
