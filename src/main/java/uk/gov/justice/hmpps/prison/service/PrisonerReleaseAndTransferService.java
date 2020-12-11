package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOut;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.UserRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementTypeAndReason.Pk;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
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
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TRN;

@Service
@Transactional
@Validated
@Slf4j
@AllArgsConstructor
public class PrisonerReleaseAndTransferService {

    private final OffenderBookingRepository offenderBookingRepository;
    private final AgencyLocationRepository agencyLocationRepository;
    private final ExternalMovementRepository externalMovementRepository;
    private final ReferenceCodeRepository<MovementType> movementTypeRepository;
    private final ReferenceCodeRepository<MovementReason> movementReasonRepository;
    private final BedAssignmentHistoriesRepository bedAssignmentHistoriesRepository;
    private final AgencyInternalLocationRepository agencyInternalLocationRepository;
    private final MovementTypeAndReasonRespository movementTypeAndReasonRespository;
    private final OffenderSentenceAdjustmentRepository offenderSentenceAdjustmentRepository;
    private final OffenderKeyDateAdjustmentRepository offenderKeyDateAdjustmentRepository;
    private final CaseNoteRepository caseNoteRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authenticationFacade;

    public void releasePrisoner(final String prisonerIdentifier, final RequestToReleasePrisoner requestToReleasePrisoner) {
        final OffenderBooking booking = getAndCheckOffenderBooking(prisonerIdentifier);

        final var movementReasonCode = requestToReleasePrisoner.getMovementReasonCode();
        final AgencyLocation toLocation = checkMovementTypes(REL.getCode(), movementReasonCode, AgencyLocation.OUT);

        // Generate the external movement out
        final var movementReason = movementReasonRepository.findById(MovementReason.pk(movementReasonCode)).orElseThrow(EntityNotFoundException.withMessage("No movement reason %s found", movementReasonCode));

        final var releaseDateTime = getAndCheckMovementTime(requestToReleasePrisoner.getReleaseTime(), booking.getBookingId());
        // set previous active movements to false
        final Long bookingId = setPreviousMovementsToInactive(booking);

        createOutMovement(booking, REL, movementReason, toLocation, releaseDateTime, requestToReleasePrisoner.getCommentText(), null);

        // generate the release case note
        generateReleaseNote(booking, releaseDateTime, movementReason);

        // Update occupancy (recursively)
        updateBeds(booking, releaseDateTime);

        deactivateSentences(bookingId);

        // update the booking record
        booking.setInOutStatus("OUT");
        booking.setActiveFlag("N");
        booking.setBookingStatus("C");
        booking.setLivingUnitMv(null);
        booking.setAssignedLivingUnit(null);
        booking.setLocation(toLocation);
        booking.setBookingEndDate(releaseDateTime);
        booking.setStatusReason(REL.getCode() + "-" + movementReasonCode);
        booking.setCommStatus(null);

    }

    public void transferOutPrisoner(final String prisonerIdentifier, final RequestToTransferOut requestToTransferOut) {
        // check that prisoner is active in
        final OffenderBooking booking = getAndCheckOffenderBooking(prisonerIdentifier);

        final AgencyLocation toLocation = checkMovementTypes(TRN.getCode(), requestToTransferOut.getTransferReasonCode(), AgencyLocation.TRN);

        // Generate the external movement out
        final var movementReason = movementReasonRepository.findById(MovementReason.pk(requestToTransferOut.getTransferReasonCode())).orElseThrow(EntityNotFoundException.withMessage("No movement reason %s found", requestToTransferOut.getTransferReasonCode()));

        final var transferDateTime = getAndCheckMovementTime(requestToTransferOut.getMovementTime(), booking.getBookingId());
        // set previous active movements to false
        setPreviousMovementsToInactive(booking);

        createOutMovement(booking, TRN, movementReason, toLocation, transferDateTime, requestToTransferOut.getCommentText(), requestToTransferOut.getEscortType());
        updateBeds(booking, transferDateTime);

        // update the booking record
        booking.setInOutStatus(TRN.getCode());
        booking.setActiveFlag("N");
        booking.setBookingStatus("O");
        booking.setLivingUnitMv(null);
        booking.setAssignedLivingUnit(null);
        booking.setLocation(toLocation);
        booking.setCreateLocation(toLocation);
        booking.setStatusReason(TRN.getCode() + "-" + requestToTransferOut.getTransferReasonCode());
        booking.setCommStatus(null);
    }

    public void transferInPrisoner(final String offenderNo, final RequestToTransferIn requestToTransferIn) {

    }

    private LocalDateTime getAndCheckMovementTime(final LocalDateTime movementTime, final Long bookingId) {
        final var now = LocalDateTime.now();
        if (movementTime != null) {
            if (movementTime.isAfter(now)) {
                throw new BadRequestException("Transfer cannot be done in the future");
            }

            externalMovementRepository.findAllByBookingIdAndActiveFlag(bookingId, ActiveFlag.Y).forEach(
                    movement -> { if (movementTime.isBefore(movement.getMovementTime())) {
                        throw new BadRequestException("Movement cannot be before the previous active movement");
                    }
                }
            );

            return movementTime;
        }
        return now;
    }


    private void createOutMovement(final OffenderBooking booking, final ReferenceCode.Pk movementCode, final MovementReason movementReason, final AgencyLocation toLocation, final LocalDateTime movementTime, final String commentText, final String escortText) {
        final var releaseMovement = ExternalMovement.builder()
            .bookingId(booking.getBookingId())
            .movementSequence(externalMovementRepository.getLatestMovementSequence(booking.getBookingId()) + 1)
            .movementDate(movementTime.toLocalDate())
            .movementTime(movementTime)
            .movementType(movementTypeRepository.findById(movementCode).orElseThrow(EntityNotFoundException.withMessage("No %s movement type found", movementCode)))
            .movementReason(movementReason)
            .movementDirection(MovementDirection.OUT)
            .fromAgency(booking.getLocation())
            .toAgency(toLocation)
            .escortText(escortText)
            .activeFlag(ActiveFlag.Y)
            .commentText(commentText)
            .build();
        externalMovementRepository.save(releaseMovement);
    }

    private Long setPreviousMovementsToInactive(final OffenderBooking booking) {
        final var bookingId = booking.getBookingId();
        externalMovementRepository.findAllByBookingIdAndActiveFlag(bookingId, ActiveFlag.Y)
            .forEach(m -> m.setActiveFlag(ActiveFlag.N));
        return bookingId;
    }

    private AgencyLocation checkMovementTypes(final String movementCode, final String reasonCode, final String toLocationId) {
        final var toLocation = agencyLocationRepository.findById(toLocationId).orElseThrow(EntityNotFoundException.withMessage("No %s agency found", toLocationId));

        final var movementTypeAndReason = Pk.builder().type(movementCode).reasonCode(reasonCode).build();

        movementTypeAndReasonRespository.findById(movementTypeAndReason)
            .orElseThrow(EntityNotFoundException.withMessage("No movement type found for {}", movementTypeAndReason));
        return toLocation;
    }

    private OffenderBooking getAndCheckOffenderBooking(final String prisonerIdentifier) {
        // check that prisoner is active in
        final var optionalOffenderBooking = offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(prisonerIdentifier, 1);

        final var booking = optionalOffenderBooking.orElseThrow(EntityNotFoundException.withMessage("No bookings found for prisoner number %s", prisonerIdentifier));

        if (!booking.getActiveFlag().equals("Y")) {
            throw new BadRequestException("Prisoner is not currently active");
        }

        if (!booking.getInOutStatus().equals("IN")) {
            throw new BadRequestException("Prisoner is not currently IN");
        }
        return booking;
    }

    private void generateReleaseNote(final OffenderBooking booking, final LocalDateTime releaseDateTime, final MovementReason movementReason) {
        final var currentUsername = authenticationFacade.getCurrentUsername();
        final var userDetail = userRepository.findByUsername(currentUsername).orElseThrow(EntityNotFoundException.withId(currentUsername));
        final var newCaseNote = NewCaseNote.builder()
            .type("PRISON")
            .subType("RELEASE")
            .text(String.format("Released from %s for reason: %s.", booking.getLocation().getDescription(), movementReason.getDescription()))
            .occurrenceDateTime(releaseDateTime)
            .build();
        caseNoteRepository.createCaseNote(booking.getBookingId(), newCaseNote, "AUTO", currentUsername, userDetail.getStaffId());
    }

    private void incrementCurrentOccupancy(final AgencyInternalLocation assignedLivingUnit) {
        if (assignedLivingUnit != null) {
            assignedLivingUnit.incrementCurrentOccupancy();
            agencyInternalLocationRepository.save(assignedLivingUnit);
            incrementCurrentOccupancy(assignedLivingUnit.getParentLocation());
        }
    }


    private void updateBeds(final OffenderBooking booking, final LocalDateTime releaseDateTime) {
        // Update occupancy (recursively)
        incrementCurrentOccupancy(booking.getAssignedLivingUnit());

        // Update Bed Assignment
        bedAssignmentHistoriesRepository.findByBedAssignmentHistoryPKOffenderBookingIdAndBedAssignmentHistoryPKSequence(booking.getBookingId(),
            bedAssignmentHistoriesRepository.getMaxSeqForBookingId(booking.getBookingId())).ifPresent(b -> {
            if (b.getAssignmentEndDate() == null && b.getAssignmentEndDateTime() == null) {
                b.setAssignmentEndDate(releaseDateTime.toLocalDate());
                b.setAssignmentEndDateTime(releaseDateTime);
            }
        });
    }

    private void deactivateSentences(final Long bookingId) {

        offenderSentenceAdjustmentRepository.findAllByOffenderBookIdAndActiveFlag(bookingId, ActiveFlag.Y)
            .forEach(s -> s.setActiveFlag(ActiveFlag.N));

        offenderKeyDateAdjustmentRepository.findAllByOffenderBookIdAndActiveFlag(bookingId, ActiveFlag.Y)
            .forEach(s -> s.setActiveFlag(ActiveFlag.N));
    }


}
