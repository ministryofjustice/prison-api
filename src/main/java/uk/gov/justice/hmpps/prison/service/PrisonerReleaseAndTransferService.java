package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.IepLevelAndComment;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOut;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.UserRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement.PK;
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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.IepLevelRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MovementTypeAndReasonRespository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderKeyDateAdjustmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderNoPayPeriodRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderPayStatusRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection.IN;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.ADM;
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
    private final OffenderNoPayPeriodRepository offenderNoPayPeriodRepository;
    private final OffenderPayStatusRepository offenderPayStatusRepository;
    private final BookingRepository bookingRepository;
    private final IepLevelRepository iepLevelRepository;
    private final FinanceRepository financeRepository;

    private final Environment env;

    public void releasePrisoner(final String prisonerIdentifier, final RequestToReleasePrisoner requestToReleasePrisoner) {
        final OffenderBooking booking = getAndCheckOffenderBooking(prisonerIdentifier);

        final var movementReasonCode = requestToReleasePrisoner.getMovementReasonCode();
        checkMovementTypes(REL.getCode(), movementReasonCode);

        // Generate the external movement out
        final var movementReason = movementReasonRepository.findById(MovementReason.pk(movementReasonCode)).orElseThrow(EntityNotFoundException.withMessage("No movement reason %s found", movementReasonCode));

        final var releaseDateTime = getAndCheckMovementTime(requestToReleasePrisoner.getReleaseTime(), booking.getBookingId());
        // set previous active movements to false
        final Long bookingId = setPreviousMovementsToInactive(booking);

        final var toLocation = agencyLocationRepository.findById(AgencyLocation.OUT).orElseThrow(EntityNotFoundException.withMessage("No %s agency found", AgencyLocation.OUT));

        createOutMovement(booking, REL, movementReason, toLocation, releaseDateTime, requestToReleasePrisoner.getCommentText(), null);

        // generate the release case note
        generateReleaseNote(booking, releaseDateTime, movementReason);

        // Update occupancy (recursively)
        updateBeds(booking, releaseDateTime);

        deactivateSentences(bookingId);

        updatePayPeriods(bookingId, releaseDateTime.toLocalDate());

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

        checkMovementTypes(TRN.getCode(), requestToTransferOut.getTransferReasonCode());

        // Generate the external movement out
        final var movementReason = movementReasonRepository.findById(MovementReason.pk(requestToTransferOut.getTransferReasonCode())).orElseThrow(EntityNotFoundException.withMessage("No movement reason %s found", requestToTransferOut.getTransferReasonCode()));

        final var transferDateTime = getAndCheckMovementTime(requestToTransferOut.getMovementTime(), booking.getBookingId());
        // set previous active movements to false
        setPreviousMovementsToInactive(booking);

        final var toLocation = agencyLocationRepository.findById(requestToTransferOut.getToLocation()).orElseThrow(EntityNotFoundException.withMessage("No %s agency found", requestToTransferOut.getToLocation()));

        createOutMovement(booking, TRN, movementReason, toLocation, transferDateTime, requestToTransferOut.getCommentText(), requestToTransferOut.getEscortType());
        updateBeds(booking, transferDateTime);
        updatePayPeriods(booking.getBookingId(), transferDateTime.toLocalDate());

        final var trnLocation = agencyLocationRepository.findById(TRN.getCode()).orElseThrow(EntityNotFoundException.withMessage("No %s agency found", TRN.getCode()));

        // update the booking record
        booking.setInOutStatus(TRN.getCode());
        booking.setActiveFlag("N");
        booking.setBookingStatus("O");
        booking.setLivingUnitMv(null);
        booking.setAssignedLivingUnit(null);
        booking.setLocation(trnLocation);
        booking.setCreateLocation(trnLocation);
        booking.setStatusReason(TRN.getCode() + "-" + requestToTransferOut.getTransferReasonCode());
        booking.setCommStatus(null);
    }

    public void transferInPrisoner(final String prisonerIdentifier, final RequestToTransferIn requestToTransferIn) {
        final OffenderBooking booking = getOffenderBooking(prisonerIdentifier);

        if (!booking.getActiveFlag().equals("Y")) {
            throw new BadRequestException("Prisoner is not currently active");
        }

        if (!booking.getInOutStatus().equals("TRN")) {
            throw new BadRequestException("Prisoner is not currently being transferred");
        }

        final var latestMovementSequence = externalMovementRepository.getLatestMovementSequence(booking.getBookingId());
        final var latestExternalMovement = externalMovementRepository.findById(new PK(booking.getBookingId(), latestMovementSequence)).orElseThrow(EntityNotFoundException.withMessage("Transfer record not found"));

        if (!latestExternalMovement.getMovementType().getCode().equals(TRN.getCode())) {
            throw new BadRequestException("Latest movement not a transfer");
        }

        if (!latestExternalMovement.getActiveFlag().isActive()) {
            throw new BadRequestException("Transfer not active");
        }

        final var internalLocation = requestToTransferIn.getCellLocation() != null ? requestToTransferIn.getCellLocation() : latestExternalMovement.getToAgency().getId() + "-" + "RECP";

        final var cellLocation = agencyInternalLocationRepository.findOneByDescription(internalLocation).orElseThrow(EntityNotFoundException.withMessage(format("%s cell location not found", internalLocation)));

        booking.setInOutStatus(IN.name());
        booking.setActiveFlag("Y");
        booking.setBookingStatus("O");
        booking.setAssignedLivingUnit(cellLocation);
        booking.setBookingEndDate(null);
        booking.setLocation(latestExternalMovement.getToAgency());

        checkMovementTypes(ADM.getCode(), "INT");

        // Generate the external movement out
        final var movementReason = movementReasonRepository.findById(MovementReason.pk("INT")).orElseThrow(EntityNotFoundException.withMessage("No movement reason %s found", "INT"));

        final var receiveTime = getAndCheckMovementTime(requestToTransferIn.getReceiveTime(), booking.getBookingId());
        // set previous active movements to false
        setPreviousMovementsToInactive(booking);

        createInMovement(booking, ADM, movementReason, latestExternalMovement.getFromAgency(), latestExternalMovement.getToAgency(), receiveTime, requestToTransferIn.getCommentText(), null);

        //Create Bed History
        bedAssignmentHistoriesRepository.save(BedAssignmentHistory.builder()
            .bedAssignmentHistoryPK(new BedAssignmentHistoryPK(booking.getBookingId(), bedAssignmentHistoriesRepository.getMaxSeqForBookingId(booking.getBookingId()) + 1))
            .livingUnitId(cellLocation.getLocationId())
            .assignmentDate(receiveTime.toLocalDate())
            .assignmentDateTime(receiveTime)
            .assignmentReason(ADM.getCode())
            .offenderBooking(booking)
            .build());

        if (env.acceptsProfiles(Profiles.of("nomis"))) { // check is running on a real NOMIS db
            // Create Trust Account
            financeRepository.createTrustAccount(latestExternalMovement.getToAgency().getId(), booking.getBookingId(), booking.getRootOffenderId(), latestExternalMovement.getFromAgency().getId(),
                movementReason.getCode(), null, null, latestExternalMovement.getToAgency().getId());
        }

        // Create IEP levels
        iepLevelRepository.findByAgencyLocationIdAndDefaultFlag(booking.getLocation().getId(), "Y")
            .stream().findFirst().ifPresentOrElse(
            iepLevel -> {
                bookingRepository.addIepLevel(booking.getBookingId(), authenticationFacade.getCurrentUsername(),
                    IepLevelAndComment.builder().iepLevel(iepLevel.getIepLevel()).comment(format("Admission to %s", booking.getLocation().getDescription())).build(), receiveTime);
            },
            () -> { throw new BadRequestException("No default IEP level found"); } );

        // create Admission case note
        generateAdmissionNote(booking.getBookingId(), latestExternalMovement.getFromAgency(), latestExternalMovement.getToAgency(), receiveTime, movementReason);
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
            .reportingDate(movementTime.toLocalDate())
            .fromAgency(booking.getLocation())
            .toAgency(toLocation)
            .escortText(escortText)
            .activeFlag(ActiveFlag.Y)
            .commentText(commentText)
            .build();
        externalMovementRepository.save(releaseMovement);
    }

    private void createInMovement(final OffenderBooking booking, final ReferenceCode.Pk movementCode, final MovementReason movementReason, final AgencyLocation fromLocation, final AgencyLocation toLocation, final LocalDateTime movementTime, final String commentText, final String escortText) {
        final var releaseMovement = ExternalMovement.builder()
            .bookingId(booking.getBookingId())
            .movementSequence(externalMovementRepository.getLatestMovementSequence(booking.getBookingId()) + 1)
            .movementDate(movementTime.toLocalDate())
            .movementTime(movementTime)
            .movementType(movementTypeRepository.findById(movementCode).orElseThrow(EntityNotFoundException.withMessage("No %s movement type found", movementCode)))
            .movementReason(movementReason)
            .movementDirection(MovementDirection.IN)
            .fromAgency(fromLocation)
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

    private void checkMovementTypes(final String movementCode, final String reasonCode) {
        final var movementTypeAndReason = Pk.builder().type(movementCode).reasonCode(reasonCode).build();

        movementTypeAndReasonRespository.findById(movementTypeAndReason)
            .orElseThrow(EntityNotFoundException.withMessage("No movement type found for {}", movementTypeAndReason));
    }

    private OffenderBooking getAndCheckOffenderBooking(final String prisonerIdentifier) {
        // check that prisoner is active in
        final OffenderBooking booking = getOffenderBooking(prisonerIdentifier);

        if (!booking.getActiveFlag().equals("Y")) {
            throw new BadRequestException("Prisoner is not currently active");
        }

        if (!booking.getInOutStatus().equals("IN")) {
            throw new BadRequestException("Prisoner is not currently IN");
        }
        return booking;
    }

    private OffenderBooking getOffenderBooking(final String prisonerIdentifier) {
        final var optionalOffenderBooking = offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(prisonerIdentifier, 1);

        final var booking = optionalOffenderBooking.orElseThrow(EntityNotFoundException.withMessage("No bookings found for prisoner number %s", prisonerIdentifier));
        return booking;
    }

    private void generateReleaseNote(final OffenderBooking booking, final LocalDateTime releaseDateTime, final MovementReason movementReason) {
        final var currentUsername = authenticationFacade.getCurrentUsername();
        final var userDetail = userRepository.findByUsername(currentUsername).orElseThrow(EntityNotFoundException.withId(currentUsername));
        final var newCaseNote = NewCaseNote.builder()
            .type("PRISON")
            .subType("RELEASE")
            .text(format("Released from %s for reason: %s.", booking.getLocation().getDescription(), movementReason.getDescription()))
            .occurrenceDateTime(releaseDateTime)
            .build();
        caseNoteRepository.createCaseNote(booking.getBookingId(), newCaseNote, "AUTO", currentUsername, userDetail.getStaffId());
    }

    private void generateAdmissionNote(final Long bookinId, final AgencyLocation fromLocation, final AgencyLocation toLocation, final LocalDateTime admissionDateTime, final MovementReason admissionReason) {
        final var currentUsername = authenticationFacade.getCurrentUsername();
        final var userDetail = userRepository.findByUsername(currentUsername).orElseThrow(EntityNotFoundException.withId(currentUsername));
        final var newCaseNote = NewCaseNote.builder()
            .type("TRANSFER")
            .subType("FROMTOL")
            .text(format("Offender admitted to %s for reason: %s from %s.", toLocation.getDescription(), admissionReason.getDescription(), fromLocation.getDescription()))
            .occurrenceDateTime(admissionDateTime)
            .build();
        caseNoteRepository.createCaseNote(bookinId, newCaseNote, "AUTO", currentUsername, userDetail.getStaffId());
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

    void updatePayPeriods(Long bookingId, LocalDate movementDate) {

        /*
         * UPDATE OFFENDER_PAY_STATUSES OPS SET OPS.END_DATE = TRUNC (SYSDATE)
         * WHERE
         *  OPS.OFFENDER_BOOK_ID = :B1 AND ( OPS.END_DATE > TRUNC (SYSDATE) OR
         *   OPS.END_DATE IS NULL) */

        final var now = LocalDate.now();
        offenderPayStatusRepository.findAllByBookingId(bookingId)
            .forEach(p -> {
                if (p.getEndDate() == null || p.getEndDate().isAfter(now)) {
                    p.setEndDate(now);
                }
            });

        /*
         *   UPDATE OFFENDER_NO_PAY_PERIODS ONPP SET ONPP.END_DATE = TRUNC (SYSDATE)
         * WHERE
         *  ONPP.OFFENDER_BOOK_ID = :B1 AND ( ONPP.END_DATE > TRUNC (SYSDATE) OR
         *   ONPP.END_DATE IS NULL)
         *
         *   UPDATE OFFENDER_NO_PAY_PERIODS SET END_DATE = GREATEST(START_DATE, :B2 )
         * WHERE
         *  OFFENDER_BOOK_ID = :B1 AND TRUNC(SYSDATE) BETWEEN START_DATE AND
         *   NVL(END_DATE, TRUNC(SYSDATE))
         *
         *   UPDATE OFFENDER_NO_PAY_PERIODS SET END_DATE = START_DATE
         * WHERE
         *  OFFENDER_BOOK_ID = :B1 AND START_DATE > TRUNC(SYSDATE)
         */

        offenderNoPayPeriodRepository.findAllByBookingId(bookingId)
            .forEach(p -> {
                if (p.getEndDate() == null || p.getEndDate().isAfter(now)) {
                    p.setEndDate(now);
                }

                if (now.compareTo(p.getStartDate()) >= 0 && now.compareTo(p.getEndDate() != null ? p.getEndDate() : now) < 0) {
                    p.setEndDate(p.getStartDate().isBefore(movementDate) ? movementDate : p.getStartDate());
                }

                if (p.getStartDate().isAfter(now)) {
                    p.setEndDate(p.getStartDate());
                }
            });
    }
}
