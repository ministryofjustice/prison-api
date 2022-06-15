package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn;
import uk.gov.justice.hmpps.prison.api.model.RequestForNewBooking;
import uk.gov.justice.hmpps.prison.api.model.RequestForTemporaryAbsenceArrival;
import uk.gov.justice.hmpps.prison.api.model.RequestToDischargePrisoner;
import uk.gov.justice.hmpps.prison.api.model.RequestToRecall;
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOut;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToCourt;
import uk.gov.justice.hmpps.prison.exception.CustomErrorCodes;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementTypeAndReason.Pk;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AvailablePrisonIepLevelRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CopyTableRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ImprisonmentStatusRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MovementTypeAndReasonRespository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderKeyDateAdjustmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderNoPayPeriodRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderPayStatusRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileTypeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.repository.storedprocs.CopyProcs.CopyBookData;
import uk.gov.justice.hmpps.prison.repository.storedprocs.OffenderAdminProcs.GenerateNewBookingNo;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.transfer.PrisonTransferService;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import static java.lang.String.format;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection.IN;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.AWAIT_REMOVAL_TO_PSY_HOSPITAL;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.DISCHARGE_TO_PSY_HOSPITAL;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.ADM;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.CRT;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.REL;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TAP;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TRN;

@Service
@Transactional
@Validated
@Slf4j
@AllArgsConstructor
public class PrisonerReleaseAndTransferService {

    public static final String PSY_HOSP_FROM_PRISON = "S48MHA";

    private final OffenderBookingRepository offenderBookingRepository;
    private final OffenderRepository offenderRepository;
    private final AgencyLocationRepository agencyLocationRepository;
    private final ExternalMovementRepository externalMovementRepository;
    private final ReferenceCodeRepository<MovementType> movementTypeRepository;
    private final ReferenceCodeRepository<AgencyLocationType> agencyLocationTypeRepository;
    private final ReferenceCodeRepository<MovementReason> movementReasonRepository;
    private final BedAssignmentHistoriesRepository bedAssignmentHistoriesRepository;
    private final AgencyInternalLocationRepository agencyInternalLocationRepository;
    private final MovementTypeAndReasonRespository movementTypeAndReasonRespository;
    private final OffenderSentenceAdjustmentRepository offenderSentenceAdjustmentRepository;
    private final OffenderKeyDateAdjustmentRepository offenderKeyDateAdjustmentRepository;
    private final OffenderCaseNoteRepository caseNoteRepository;
    private final AuthenticationFacade authenticationFacade;
    private final OffenderNoPayPeriodRepository offenderNoPayPeriodRepository;
    private final OffenderPayStatusRepository offenderPayStatusRepository;
    private final AvailablePrisonIepLevelRepository availablePrisonIepLevelRepository;
    private final FinanceRepository financeRepository;
    private final ImprisonmentStatusRepository imprisonmentStatusRepository;
    private final ReferenceCodeRepository<CaseNoteType> caseNoteTypeReferenceCodeRepository;
    private final ReferenceCodeRepository<CaseNoteSubType> caseNoteSubTypeReferenceCodeRepository;
    private final ProfileCodeRepository profileCodeRepository;
    private final ProfileTypeRepository profileTypeRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final GenerateNewBookingNo generateNewBookingNo;
    private final CopyTableRepository copyTableRepository;
    private final CopyBookData copyBookData;
    private final OffenderTransformer offenderTransformer;
    private final OffenderProgramProfileRepository offenderProgramProfileRepository;
    private final EntityManager entityManager;
    private final CourtEventRepository courtEventRepository;
    private final OffenderIndividualScheduleRepository offenderIndividualScheduleRepository;
    private final ReferenceCodeRepository<EventStatus> eventStatusRepository;

    private final PrisonTransferService prisonTransferService;

    private final Environment env;

    public InmateDetail releasePrisoner(final String prisonerIdentifier, final RequestToReleasePrisoner requestToReleasePrisoner, RequestToDischargePrisoner requestToDischargePrisoner) {
        final OffenderBooking booking = getAndCheckOffenderBooking(prisonerIdentifier, requestToDischargePrisoner != null);

        final var movementReasonCode = requestToReleasePrisoner.getMovementReasonCode();
        checkMovementTypes(REL.getCode(), movementReasonCode);

        // Generate the external movement out
        final var movementReason = movementReasonRepository.findById(MovementReason.pk(movementReasonCode)).orElseThrow(EntityNotFoundException.withMessage(format("No movement reason %s found", movementReasonCode)));

        final var releaseDateTime = getAndCheckMovementTime(requestToReleasePrisoner.getReleaseTime(), booking.getBookingId());
        // set previous active movements to false
        deactivatePreviousMovements(booking);

        final var supportingPrison = requestToDischargePrisoner != null && requestToDischargePrisoner.getSupportingPrisonId() != null ? agencyLocationRepository.findById(requestToDischargePrisoner.getSupportingPrisonId()).orElseThrow(EntityNotFoundException.withMessage(format("No %s agency found", requestToDischargePrisoner.getSupportingPrisonId()))) : booking.getLocation();
        final var toLocation = agencyLocationRepository.findById(requestToReleasePrisoner.getToLocationCode()).orElseThrow(EntityNotFoundException.withMessage(format("No %s agency found", requestToReleasePrisoner.getToLocationCode())));

        createOutMovement(booking, REL, movementReason, supportingPrison, toLocation, releaseDateTime, requestToReleasePrisoner.getCommentText(), null);

        // generate the release case note
        generateReleaseNote(booking, releaseDateTime, movementReason);

        updateBedAssignmentHistory(booking, releaseDateTime);

        deactivateSentences(booking.getBookingId());

        updatePayPeriods(booking.getBookingId(), releaseDateTime.toLocalDate());

        deactivateEvents(booking.getBookingId());

        // update the booking record
        booking.setInOutStatus("OUT");
        booking.setActive(false);
        booking.setBookingStatus("C");
        booking.setLivingUnitMv(null);
        booking.setAssignedLivingUnit(null);
        booking.setLocation(agencyLocationRepository.findById(AgencyLocation.OUT).orElseThrow(EntityNotFoundException.withMessage(format("No %s agency found", AgencyLocation.OUT))));
        booking.setBookingEndDate(releaseDateTime);
        booking.setStatusReason(REL.getCode() + "-" + movementReasonCode);
        booking.setCommStatus(null);

        return offenderTransformer.transform(booking);
    }

    @VerifyOffenderAccess(overrideRoles = {"RELEASE_PRISONER"})
    public InmateDetail dischargeToHospital(final String prisonerIdentifier, final RequestToDischargePrisoner requestToDischargePrisoner) {
        final var prisoner = offenderRepository.findOffenderByNomsId(prisonerIdentifier).orElseThrow(EntityNotFoundException.withMessage(format("No prisoner found for prisoner number %s", prisonerIdentifier)));

        if (prisoner.getBookings().isEmpty()) {
            log.debug("Prisoner booking not yet created, need to create one");
            newBooking(prisonerIdentifier, RequestForNewBooking.builder()
                .bookingInTime(requestToDischargePrisoner.getDischargeTime())
                .fromLocationId(requestToDischargePrisoner.getFromLocationId())
                .prisonId(requestToDischargePrisoner.getSupportingPrisonId())
                .movementReasonCode(movementReasonRepository.findById(AWAIT_REMOVAL_TO_PSY_HOSPITAL).orElseThrow(EntityNotFoundException.withMessage(format("Movement Reason %s not found", AWAIT_REMOVAL_TO_PSY_HOSPITAL))).getCode()) // Awaiting Removal to Psychiatric Hospital TODO: config?
                .imprisonmentStatus(imprisonmentStatusRepository.findByStatusAndActive(PSY_HOSP_FROM_PRISON, true).orElseThrow(EntityNotFoundException.withMessage(format("No imprisonment status %s found", PSY_HOSP_FROM_PRISON))).getStatus()) // Psychiatric Hospital from Prison (RX) TODO: config?
                .build());
        }

        final var toLocation = agencyLocationRepository.findById(requestToDischargePrisoner.getHospitalLocationCode()).orElseThrow(EntityNotFoundException.withMessage(format("No %s agency found", requestToDischargePrisoner.getHospitalLocationCode())));
        if (!toLocation.isHospital()) {
            throw EntityNotFoundException.withMessage(format("%s is not a hospital", toLocation.getDescription()));
        }

        final var offenderBooking = getOffenderBooking(prisonerIdentifier);
        final var lastMovement = offenderBooking.getLastMovement().orElse(null);
        final var commentText = "Psychiatric Hospital Discharge to " + toLocation.getDescription();
        if (lastMovement != null && REL.getCode().equals(lastMovement.getMovementType().getCode())) {
            // just update the external movement
            lastMovement.setMovementReason(movementReasonRepository.findById(DISCHARGE_TO_PSY_HOSPITAL).orElseThrow(EntityNotFoundException.withMessage(format("No movement reason %s found", DISCHARGE_TO_PSY_HOSPITAL))));
            lastMovement.setToAgency(toLocation);
            lastMovement.setCommentText(commentText);
            lastMovement.setFromAgency(agencyLocationRepository.findById(requestToDischargePrisoner.getSupportingPrisonId()).orElseThrow(EntityNotFoundException.withMessage(format("No %s agency found", requestToDischargePrisoner.getSupportingPrisonId()))));
            offenderBooking.setStatusReason(REL.getCode() + "-" + DISCHARGE_TO_PSY_HOSPITAL.getCode());
        } else {
            releasePrisoner(prisonerIdentifier, RequestToReleasePrisoner.builder()
                .commentText(commentText)
                .releaseTime(requestToDischargePrisoner.getDischargeTime())
                .movementReasonCode(DISCHARGE_TO_PSY_HOSPITAL.getCode())
                .toLocationCode(toLocation.getId())
                .build(), requestToDischargePrisoner);
        }
        return offenderTransformer.transform(offenderBooking);
    }

    @VerifyOffenderAccess(overrideRoles = {"TRANSFER_PRISONER"})
    public InmateDetail transferOutPrisoner(final String prisonerIdentifier, final RequestToTransferOut requestToTransferOut) {
        // check that prisoner is active in
        final OffenderBooking booking = getAndCheckOffenderBooking(prisonerIdentifier, false);

        checkMovementTypes(TRN.getCode(), requestToTransferOut.getTransferReasonCode());

        // Generate the external movement out
        final var movementReason = movementReasonRepository.findById(MovementReason.pk(requestToTransferOut.getTransferReasonCode())).orElseThrow(EntityNotFoundException.withMessage(format("No movement reason %s found", requestToTransferOut.getTransferReasonCode())));

        final var transferDateTime = getAndCheckMovementTime(requestToTransferOut.getMovementTime(), booking.getBookingId());
        // set previous active movements to false
        deactivatePreviousMovements(booking);

        final var agencyLocationType = agencyLocationTypeRepository.findById(AgencyLocationType.INST).orElseThrow(EntityNotFoundException.withMessage(format("Agency Location Type of %s not Found", AgencyLocationType.INST.getCode())));
        final var toLocation = agencyLocationRepository.findByIdAndTypeAndActiveAndDeactivationDateIsNull(requestToTransferOut.getToLocation(), agencyLocationType, true).orElseThrow(EntityNotFoundException.withMessage(format("No %s agency found", requestToTransferOut.getToLocation())));

        createOutMovement(booking, TRN, movementReason, booking.getLocation(), toLocation, transferDateTime, requestToTransferOut.getCommentText(), requestToTransferOut.getEscortType());
        updateBedAssignmentHistory(booking, transferDateTime);
        updatePayPeriods(booking.getBookingId(), transferDateTime.toLocalDate());

        final var trnLocation = agencyLocationRepository.findById(TRN.getCode()).orElseThrow(EntityNotFoundException.withMessage(format("No %s agency found", TRN.getCode())));

        // update the booking record
        booking.setInOutStatus(TRN.getCode());
        booking.setActive(false);
        booking.setBookingStatus("O");
        booking.setLivingUnitMv(null);
        booking.setAssignedLivingUnit(null);
        booking.setLocation(trnLocation);
        booking.setCreateLocation(trnLocation);
        booking.setStatusReason(TRN.getCode() + "-" + requestToTransferOut.getTransferReasonCode());
        booking.setCommStatus(null);

        return offenderTransformer.transform(booking);
    }

    @VerifyOffenderAccess(overrideRoles = {"TRANSFER_PRISONER_ALPHA"})
    public InmateDetail transferOutPrisonerToCourt(final String prisonerIdentifier, final RequestToTransferOutToCourt requestToTransferOutToCourt) {
        // NB This API requires further validation before it can be used for production - it is currently used just to generate test data

        // check that prisoner is active in
        final OffenderBooking booking = getAndCheckOffenderBooking(prisonerIdentifier, false);

        checkMovementTypes(CRT.getCode(), requestToTransferOutToCourt.getTransferReasonCode());

        // Generate the external movement out
        final var movementReason = movementReasonRepository.findById(MovementReason.pk(requestToTransferOutToCourt.getTransferReasonCode())).orElseThrow(EntityNotFoundException.withMessage(format("No movement reason %s found", requestToTransferOutToCourt.getTransferReasonCode())));

        final var transferDateTime = getAndCheckMovementTime(requestToTransferOutToCourt.getMovementTime(), booking.getBookingId());
        // set previous active movements to false
        deactivatePreviousMovements(booking);

        final var agencyLocationType = agencyLocationTypeRepository.findById(AgencyLocationType.CRT).orElseThrow(EntityNotFoundException.withMessage(format("Agency Location Type of %s not Found", AgencyLocationType.INST.getCode())));
        final var toLocation = agencyLocationRepository.findByIdAndTypeAndActiveAndDeactivationDateIsNull(requestToTransferOutToCourt.getToLocation(), agencyLocationType, true).orElseThrow(EntityNotFoundException.withMessage(format("No %s agency found", requestToTransferOutToCourt.getToLocation())));

        createOutMovement(booking, CRT, movementReason, booking.getLocation(), toLocation, transferDateTime, requestToTransferOutToCourt.getCommentText(), requestToTransferOutToCourt.getEscortType(), requestToTransferOutToCourt.getCourtEventId());
        Optional.ofNullable(requestToTransferOutToCourt.getCourtEventId()).ifPresent(id -> createScheduleCourtHearingInEvent(markCourtEventComplete(booking, id)));
        if (requestToTransferOutToCourt.isShouldReleaseBed()) {
            updateBedAssignmentHistory(booking, transferDateTime);
            booking.setLivingUnitMv(null);
            booking.setAssignedLivingUnit(agencyInternalLocationRepository.findOneByLocationCodeAndAgencyId("COURT", booking.getLocation().getId()).orElseThrow(EntityNotFoundException.withMessage(format("No COURT internal location found for %s", booking.getLocation().getId()))));
            bedAssignmentHistoriesRepository.save(BedAssignmentHistory.builder()
                .bedAssignmentHistoryPK(new BedAssignmentHistoryPK(booking.getBookingId(), bedAssignmentHistoriesRepository.getMaxSeqForBookingId(booking.getBookingId()) + 1))
                .livingUnitId(booking.getAssignedLivingUnit().getLocationId())
                .assignmentDate(transferDateTime.toLocalDate())
                .assignmentDateTime(transferDateTime)
                .assignmentReason(movementReason.getCode())
                .offenderBooking(booking)
                .build());
        }

        // update the booking record
        booking.setInOutStatus("OUT");
        booking.setActive(true);
        booking.setBookingStatus("O");
        booking.setStatusReason(CRT.getCode() + "-" + requestToTransferOutToCourt.getTransferReasonCode());
        booking.setCommStatus(null);

        return offenderTransformer.transform(booking);
    }

    private CourtEvent createScheduleCourtHearingInEvent(CourtEvent parentEvent) {
        var returnToPrisonScheduledEvent = CourtEvent
            .builder()
            .courtEventType(parentEvent.getCourtEventType())
            .courtLocation(parentEvent.getCourtLocation())
            .eventStatus(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED).orElseThrow())
            .commentText(parentEvent.getCommentText())
            .directionCode("IN")
            .eventDate(parentEvent.getEventDate())
            .offenderBooking(parentEvent.getOffenderBooking())
            .parentCourtEventId(parentEvent.getId())
            .startTime(parentEvent.getStartTime())
            .build();
        return courtEventRepository.save(returnToPrisonScheduledEvent);
    }

    private CourtEvent markCourtEventComplete(OffenderBooking booking, Long id) {
        var courtEvent = courtEventRepository.findById(id).orElseThrow(EntityNotFoundException.withMessage(format("No court event found for %s", id)));
        courtEvent.setEventStatus(eventStatusRepository.findById(EventStatus.COMPLETED).orElseThrow());
        return courtEvent;
    }

    public InmateDetail recallPrisoner(final String prisonerIdentifier, final RequestToRecall requestToRecall) {

        final OffenderBooking booking = getOffenderBooking(prisonerIdentifier);

        if (booking.isActive()) {
            throw new BadRequestException("Prisoner is currently active");
        }

        if (!booking.getInOutStatus().equals("OUT")) {
            throw new BadRequestException("Prisoner is not currently OUT");
        }

        // check from location
        final var fromLocation = getFromLocation(requestToRecall.getFromLocationId());

        // check imprisonment status
        ImprisonmentStatus imprisonmentStatus = null;
        if (requestToRecall.getImprisonmentStatus() != null) {
            imprisonmentStatus = imprisonmentStatusRepository.findByStatusAndActive(requestToRecall.getImprisonmentStatus(), true).orElseThrow(EntityNotFoundException.withMessage(format("No imprisonment status %s found", requestToRecall.getImprisonmentStatus())));
        }

        // check prison id
        final var agencyLocationType = agencyLocationTypeRepository.findById(AgencyLocationType.INST).orElseThrow(EntityNotFoundException.withMessage(format("Agency Location Type of %s not Found", AgencyLocationType.INST.getCode())));
        final var prisonToRecallTo = agencyLocationRepository.findByIdAndTypeAndActiveAndDeactivationDateIsNull(requestToRecall.getPrisonId(), agencyLocationType, true).orElseThrow(EntityNotFoundException.withMessage(format("%s prison not found", requestToRecall.getPrisonId())));

        final var internalLocation = requestToRecall.getCellLocation() != null ? requestToRecall.getCellLocation() : prisonToRecallTo.getId() + "-" + "RECP";

        final var cellLocation = agencyInternalLocationRepository.findOneByDescriptionAndAgencyId(internalLocation, prisonToRecallTo.getId()).orElseThrow(EntityNotFoundException.withMessage(format("%s cell location not found", internalLocation)));
        if (!cellLocation.hasSpace(true)) {
            throw ConflictingRequestException.withMessage(format("The cell %s does not have any available capacity", internalLocation), CustomErrorCodes.NO_CELL_CAPACITY);
        }

        booking.setInOutStatus(IN.name());
        booking.setActive(true);
        booking.setBookingStatus("O");
        booking.setAssignedLivingUnit(cellLocation);
        booking.setBookingEndDate(null);
        booking.setLocation(prisonToRecallTo);

        checkMovementTypes(ADM.getCode(), requestToRecall.getMovementReasonCode());

        final var movementReason = movementReasonRepository.findById(MovementReason.pk(requestToRecall.getMovementReasonCode())).orElseThrow(EntityNotFoundException.withMessage(format("No movement reason %s found", requestToRecall.getMovementReasonCode())));

        final var receiveTime = getAndCheckMovementTime(requestToRecall.getRecallTime(), booking.getBookingId());

        // set previous active movements to false
        deactivatePreviousMovements(booking);

        // Generate the external movement in
        createInMovement(booking, movementReason, fromLocation, prisonToRecallTo, receiveTime, "Recall");
        booking.setStatusReason(ADM.getCode() + "-" + movementReason.getCode());

        //Create Bed History
        bedAssignmentHistoriesRepository.save(BedAssignmentHistory.builder()
            .bedAssignmentHistoryPK(new BedAssignmentHistoryPK(booking.getBookingId(), bedAssignmentHistoriesRepository.getMaxSeqForBookingId(booking.getBookingId()) + 1))
            .livingUnitId(cellLocation.getLocationId())
            .assignmentDate(receiveTime.toLocalDate())
            .assignmentDateTime(receiveTime)
            .assignmentReason(ADM.getCode())
            .offenderBooking(booking)
            .build());

        if (requestToRecall.isYouthOffender()) {
            setYouthStatus(booking);
        }

        if (env.acceptsProfiles(Profiles.of("nomis"))) { // check is running on a real NOMIS db
            // Create Trust Account
            financeRepository.createTrustAccount(prisonToRecallTo.getId(), booking.getBookingId(), booking.getRootOffender().getId(), fromLocation.getId(),
                movementReason.getCode(), null, null, prisonToRecallTo.getId());
        }

        // Create IEP levels
        availablePrisonIepLevelRepository.findByAgencyLocation_IdAndDefaultIep(prisonToRecallTo.getId(), true)
            .stream().findFirst().ifPresentOrElse(
                iepLevel -> {
                    final var staff = staffUserAccountRepository.findById(authenticationFacade.getCurrentUsername()).orElseThrow(EntityNotFoundException.withId(authenticationFacade.getCurrentUsername()));
                    booking.addIepLevel(iepLevel.getIepLevel(), format("Admission to %s", prisonToRecallTo.getDescription()), receiveTime, staff);
                },
                () -> {
                    throw new BadRequestException("No default IEP level found");
                });

        //clear off old status
        setupBookingAccount(booking, fromLocation, prisonToRecallTo, receiveTime, movementReason, imprisonmentStatus);

        return offenderTransformer.transform(booking);
    }

    private AgencyLocation getFromLocation(final String fromLocationId) {
        return StringUtils.isNotBlank(fromLocationId)
            ? agencyLocationRepository.findByIdAndDeactivationDateIsNull(fromLocationId).orElseThrow(EntityNotFoundException.withMessage(format("%s is not a valid from location", fromLocationId)))
            : agencyLocationRepository.findById(AgencyLocation.OUT).orElseThrow(EntityNotFoundException.withMessage(format("%s is not a valid from location", AgencyLocation.OUT)));
    }

    public InmateDetail newBooking(final String prisonerIdentifier, final RequestForNewBooking requestForNewBooking) {

        final var offender = offenderRepository.findOffenderByNomsId(prisonerIdentifier).orElseThrow(EntityNotFoundException.withMessage(format("No prisoner found for prisoner number %s", prisonerIdentifier)));

        final var previousBooking = offender.getLatestBooking();
        previousBooking
            .ifPresent(booking -> {
                if (booking.isActive()) {
                    throw new BadRequestException("Prisoner is currently active");
                }

                if (!booking.getInOutStatus().equals("OUT")) {
                    throw new BadRequestException("Prisoner is not currently OUT");
                }
            });


        // check from location
        final var fromLocation = getFromLocation(requestForNewBooking.getFromLocationId());

        // check imprisonment status
        final var imprisonmentStatus = imprisonmentStatusRepository.findByStatusAndActive(requestForNewBooking.getImprisonmentStatus(), true).orElseThrow(EntityNotFoundException.withMessage(format("No imprisonment status %s found", requestForNewBooking.getImprisonmentStatus())));

        // check prison id
        final var receivedPrison = agencyLocationRepository.findByIdAndTypeAndActiveAndDeactivationDateIsNull(requestForNewBooking.getPrisonId(), agencyLocationTypeRepository.findById(AgencyLocationType.INST).orElseThrow(EntityNotFoundException.withMessage("Not Found")), true).orElseThrow(EntityNotFoundException.withMessage(format("%s prison not found", requestForNewBooking.getPrisonId())));

        final var internalLocation = requestForNewBooking.getCellLocation() != null ? requestForNewBooking.getCellLocation() : receivedPrison.getId() + "-" + "RECP";

        final var cellLocation = agencyInternalLocationRepository.findOneByDescriptionAndAgencyId(internalLocation, receivedPrison.getId()).orElseThrow(EntityNotFoundException.withMessage(format("%s cell location not found", internalLocation)));
        if (!cellLocation.hasSpace(true)) {
            throw ConflictingRequestException.withMessage(format("The cell %s does not have any available capacity", internalLocation), CustomErrorCodes.NO_CELL_CAPACITY);
        }

        final var currentUsername = authenticationFacade.getCurrentUsername();

        final var receiveTime = getAndCheckMovementTime(requestForNewBooking.getBookingInTime(), previousBooking.map(OffenderBooking::getBookingId).orElse(null));

        final var bookNumber = env.acceptsProfiles(Profiles.of("nomis")) ? generateNewBookingNo.executeFunction(String.class) : getRandomNumberString() + "D"; // TODO replace PL/SQL SP

        offender.getBookings().forEach(OffenderBooking::incBookingSequence);

        final var booking = offenderBookingRepository.save(
            OffenderBooking.builder()
                .bookingBeginDate(receiveTime)
                .bookNumber(bookNumber)
                .offender(offender)
                .location(receivedPrison)
                .assignedLivingUnit(cellLocation)
                .disclosureFlag("N")
                .inOutStatus(IN.name())
                .active(true)
                .bookingStatus("O")
                .youthAdultCode("N")
                .assignedStaff(staffUserAccountRepository.findById(currentUsername).orElseThrow(EntityNotFoundException.withMessage(format("No Staff found for username %s", currentUsername))).getStaff())
                .createLocation(receivedPrison)
                .bookingType("INST")
                .rootOffender(offender.getRootOffender())
                .admissionReason("NCO")
                .bookingSequence(1)
                .statusReason(ADM.getCode() + "-" + requestForNewBooking.getMovementReasonCode())
                .build()
        );
        entityManager.flush();

        checkMovementTypes(ADM.getCode(), requestForNewBooking.getMovementReasonCode());

        final var movementReason = movementReasonRepository.findById(MovementReason.pk(requestForNewBooking.getMovementReasonCode())).orElseThrow(EntityNotFoundException.withMessage(format("No movement reason %s found", requestForNewBooking.getMovementReasonCode())));

        // set previous active movements to false
        deactivatePreviousMovements(booking);

        // Generate the external movement in
        createInMovement(booking, movementReason, fromLocation, receivedPrison, receiveTime, "New Booking");

        //Create Bed History
        bedAssignmentHistoriesRepository.save(BedAssignmentHistory.builder()
            .bedAssignmentHistoryPK(new BedAssignmentHistoryPK(booking.getBookingId(), bedAssignmentHistoriesRepository.getMaxSeqForBookingId(booking.getBookingId()) + 1))
            .livingUnitId(cellLocation.getLocationId())
            .assignmentDate(receiveTime.toLocalDate())
            .assignmentDateTime(receiveTime)
            .assignmentReason(ADM.getCode())
            .offenderBooking(booking)
            .build());

        entityManager.flush();
        previousBooking.ifPresent(oldBooking -> copyTableRepository.findByOperationCodeAndMovementTypeAndActiveAndExpiryDateIsNull("COP", ADM.getCode(), true)
            .stream().findFirst().ifPresent(
                ct -> {
                    if (env.acceptsProfiles(Profiles.of("nomis"))) {
                        final var params = new MapSqlParameterSource()
                            .addValue("p_move_type", ADM.getCode())
                            .addValue("p_move_reason", movementReason.getCode())
                            .addValue("p_old_book_id", oldBooking.getBookingId())
                            .addValue("p_new_book_id", booking.getBookingId());
                        copyBookData.execute(params);
                    }
                }
            ));
        entityManager.refresh(booking);

        if (requestForNewBooking.isYouthOffender()) {
            // set youth status
            setYouthStatus(booking);
        }

        if (env.acceptsProfiles(Profiles.of("nomis"))) { // check is running on a real NOMIS db
            // Create Trust Account
            financeRepository.createTrustAccount(receivedPrison.getId(), booking.getBookingId(), booking.getRootOffender().getId(), fromLocation.getId(),
                movementReason.getCode(), null, null, receivedPrison.getId());
        }

        // Create IEP levels
        availablePrisonIepLevelRepository.findByAgencyLocation_IdAndDefaultIep(receivedPrison.getId(), true)
            .stream().findFirst().ifPresentOrElse(
                iepLevel -> {
                    final var staff = staffUserAccountRepository.findById(authenticationFacade.getCurrentUsername()).orElseThrow(EntityNotFoundException.withId(authenticationFacade.getCurrentUsername()));
                    booking.addIepLevel(iepLevel.getIepLevel(), format("Admission to %s", receivedPrison.getDescription()), receiveTime, staff);
                },
                () -> {
                    throw new BadRequestException("No default IEP level found");
                });

        setupBookingAccount(booking, fromLocation, receivedPrison, receiveTime, movementReason, imprisonmentStatus);

        return offenderTransformer.transform(booking);
    }

    private void setupBookingAccount(final OffenderBooking booking, final AgencyLocation fromLocation, final AgencyLocation receivedPrison, final LocalDateTime receiveTime, final MovementReason movementReason, final ImprisonmentStatus imprisonmentStatus) {

        // add imprisonment status
        if (imprisonmentStatus != null) {
            booking.setImprisonmentStatus(OffenderImprisonmentStatus.builder()
                .agyLocId(receivedPrison.getId())
                .imprisonmentStatus(imprisonmentStatus)
                .build(), receiveTime);
        }

        // create Admission case note
        generateAdmissionNote(booking, fromLocation, receivedPrison, receiveTime, movementReason);
    }

    private void setYouthStatus(final OffenderBooking booking) {
        final var profileType = profileTypeRepository.findByTypeAndCategoryAndActive("YOUTH", "PI", true).orElseThrow(EntityNotFoundException.withId("YOUTH"));
        final var profileCode = profileCodeRepository.findById(new ProfileCode.PK(profileType, "Y")).orElseThrow(EntityNotFoundException.withMessage("Profile Code for YOUTH and Y not found"));
        booking.add(profileType, profileCode);
    }

    public InmateDetail transferInPrisoner(final String prisonerIdentifier, final RequestToTransferIn requestToTransferIn) {
        return prisonTransferService.transferFromPrison(prisonerIdentifier, requestToTransferIn);
    }

    private LocalDateTime getAndCheckMovementTime(final LocalDateTime movementTime, final Long bookingId) {
        final var now = LocalDateTime.now();
        if (movementTime != null) {
            if (movementTime.isAfter(now)) {
                throw new BadRequestException("Transfer cannot be done in the future");
            }
            if (bookingId != null) {
                externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(bookingId, true).forEach(
                    movement -> {
                        if (movementTime.isBefore(movement.getMovementTime())) {
                            throw new BadRequestException("Movement cannot be before the previous active movement");
                        }
                    }
                );
            }

            return movementTime;
        }
        return now;
    }

    private void deactivatePreviousMovements(final OffenderBooking booking) {
        booking.setPreviousMovementsToInactive();
        // need to ensure the above updates are executed before booking updates as it invokes a trigger that updates the booking status reason
        entityManager.flush();
    }

    private void createOutMovement(final OffenderBooking booking,
                                   final ReferenceCode.Pk movementCode,
                                   final MovementReason movementReason,
                                   final AgencyLocation fromLocation,
                                   final AgencyLocation toLocation,
                                   final LocalDateTime movementTime,
                                   final String commentText,
                                   final String escortText) {
        booking.addExternalMovement(ExternalMovement.builder()
            .movementDate(movementTime.toLocalDate())
            .movementTime(movementTime)
            .movementType(movementTypeRepository.findById(movementCode).orElseThrow(EntityNotFoundException.withMessage(format("No %s movement type found", movementCode))))
            .movementReason(movementReason)
            .movementDirection(MovementDirection.OUT)
            .reportingDate(movementTime.toLocalDate())
            .fromAgency(fromLocation)
            .toAgency(toLocation)
            .escortText(escortText)
            .active(true)
            .commentText(commentText)
            .build());
    }

    private void createOutMovement(final OffenderBooking booking,
                                   final ReferenceCode.Pk movementCode,
                                   final MovementReason movementReason,
                                   final AgencyLocation fromLocation,
                                   final AgencyLocation toLocation,
                                   final LocalDateTime movementTime,
                                   final String commentText,
                                   final String escortText,
                                   final Long courtEventId) {
        booking.addExternalMovement(ExternalMovement.builder()
            .movementDate(movementTime.toLocalDate())
            .movementTime(movementTime)
            .movementType(movementTypeRepository.findById(movementCode).orElseThrow(EntityNotFoundException.withMessage(format("No %s movement type found", movementCode))))
            .movementReason(movementReason)
            .movementDirection(MovementDirection.OUT)
            .reportingDate(movementTime.toLocalDate())
            .fromAgency(fromLocation)
            .toAgency(toLocation)
            .escortText(escortText)
            .active(true)
            .commentText(commentText)
            .eventId(courtEventId)
            .build());
    }

    private void createInMovement(final OffenderBooking booking, final MovementReason movementReason, final AgencyLocation fromLocation, final AgencyLocation toLocation, final LocalDateTime movementTime, final String commentText) {
        booking.addExternalMovement(ExternalMovement.builder()
            .movementDate(movementTime.toLocalDate())
            .movementTime(movementTime)
            .movementType(movementTypeRepository.findById(MovementType.ADM).orElseThrow(EntityNotFoundException.withMessage(format("No %s movement type found", MovementType.ADM))))
            .movementReason(movementReason)
            .movementDirection(MovementDirection.IN)
            .fromAgency(fromLocation)
            .toAgency(toLocation)
            .active(true)
            .commentText(commentText)
            .build());
    }

    private void checkMovementTypes(final String movementCode, final String reasonCode) {
        final var movementTypeAndReason = Pk.builder().type(movementCode).reasonCode(reasonCode).build();

        movementTypeAndReasonRespository.findById(movementTypeAndReason)
            .orElseThrow(EntityNotFoundException.withMessage(format("No movement type found for %s", movementTypeAndReason)));
    }

    private OffenderBooking getAndCheckOffenderBooking(final String prisonerIdentifier, final boolean skipChecks) {
        // check that prisoner is active in
        final OffenderBooking booking = getOffenderBooking(prisonerIdentifier);

        if (!skipChecks) {
            if (!booking.isActive()) {
                throw new BadRequestException("Prisoner is not currently active");
            }

            if (!booking.getInOutStatus().equals("IN")) {
                throw new BadRequestException("Prisoner is not currently IN");
            }
        }
        return booking;
    }

    private OffenderBooking getOffenderBooking(final String prisonerIdentifier) {
        final var optionalOffenderBooking = offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(prisonerIdentifier, 1);

        return optionalOffenderBooking.orElseThrow(EntityNotFoundException.withMessage(format("No bookings found for prisoner number %s", prisonerIdentifier)));
    }

    private void generateReleaseNote(final OffenderBooking booking, final LocalDateTime releaseDateTime, final MovementReason movementReason) {
        final var currentUsername = authenticationFacade.getCurrentUsername();
        final var userDetail = staffUserAccountRepository.findById(currentUsername).orElseThrow(EntityNotFoundException.withId(currentUsername));

        final var newCaseNote = OffenderCaseNote.builder()
            .caseNoteText(format("Released from %s for reason: %s.", booking.getLocation().getDescription(), movementReason.getDescription()))
            .agencyLocation(booking.getLocation())
            .type(caseNoteTypeReferenceCodeRepository.findById(CaseNoteType.pk("PRISON")).orElseThrow(EntityNotFoundException.withId("PRISON")))
            .subType(caseNoteSubTypeReferenceCodeRepository.findById(CaseNoteSubType.pk("RELEASE")).orElseThrow(EntityNotFoundException.withId("RELEASE")))
            .noteSourceCode("AUTO")
            .author(userDetail.getStaff())
            .occurrenceDateTime(releaseDateTime)
            .occurrenceDate(releaseDateTime.toLocalDate())
            .amendmentFlag(false)
            .offenderBooking(booking)
            .build();
        caseNoteRepository.save(newCaseNote);
    }

    private void generateAdmissionNote(final OffenderBooking booking, final AgencyLocation fromLocation, final AgencyLocation toLocation, final LocalDateTime admissionDateTime, final MovementReason admissionReason) {
        final var currentUsername = authenticationFacade.getCurrentUsername();
        final var userDetail = staffUserAccountRepository.findById(currentUsername).orElseThrow(EntityNotFoundException.withId(currentUsername));

        final var newCaseNote = OffenderCaseNote.builder()
            .caseNoteText(format("Offender admitted to %s for reason: %s from %s.", toLocation.getDescription(), admissionReason.getDescription(), fromLocation.getDescription()))
            .agencyLocation(booking.getLocation())
            .type(caseNoteTypeReferenceCodeRepository.findById(CaseNoteType.pk("TRANSFER")).orElseThrow(EntityNotFoundException.withId("TRANSFER")))
            .subType(caseNoteSubTypeReferenceCodeRepository.findById(CaseNoteSubType.pk("FROMTOL")).orElseThrow(EntityNotFoundException.withId("FROMTOL")))
            .noteSourceCode("AUTO")
            .author(userDetail.getStaff())
            .occurrenceDateTime(admissionDateTime)
            .occurrenceDate(admissionDateTime.toLocalDate())
            .amendmentFlag(false)
            .offenderBooking(booking)
            .build();
        caseNoteRepository.save(newCaseNote);
    }

    private void updateBedAssignmentHistory(final OffenderBooking booking, final LocalDateTime releaseDateTime) {
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

        offenderSentenceAdjustmentRepository.findAllByOffenderBooking_BookingIdAndActive(bookingId, true)
            .forEach(s -> s.setActive(false));

        offenderKeyDateAdjustmentRepository.findAllByOffenderBooking_BookingIdAndActive(bookingId, true)
            .forEach(s -> s.setActive(false));
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

    private void deactivateEvents(final Long bookingId) {
        final var programProfiles = offenderProgramProfileRepository.findByOffenderBooking_BookingIdAndProgramStatus(bookingId, "ALLOC");

        programProfiles.forEach(profile -> profile.setEndDate(LocalDate.now()));
    }


    public static String getRandomNumberString() {
        // It will generate 5 digit random Number.
        // from 0 to 99999
        Random rnd = new Random();
        int number = rnd.nextInt(99999);

        // this will convert any number sequence into 6 character.
        return format("%05d", number);
    }

    public InmateDetail courtTransferIn(final String offenderNo, final RequestForCourtTransferIn requestForCourtTransferIn) {
        return prisonTransferService.transferViaCourt(offenderNo, requestForCourtTransferIn);
    }

    public InmateDetail temporaryAbsenceArrival(final String offenderNo, final RequestForTemporaryAbsenceArrival requestForTemporaryAbsenceArrival) {

        final OffenderBooking offenderBooking = this.getOffenderBooking(offenderNo);
        if (!offenderBooking.getInOutStatus().equals("OUT")) {
            throw new BadRequestException("Prisoner is not currently out");
        }
        final var latestExternalMovement = offenderBooking.getLastMovement().map(lm -> {
            if (!lm.getMovementType().getCode().equals(TAP.getCode())) {
                throw new BadRequestException("Latest movement not a temporary absence");
            }

            if (!lm.isActive()) {
                throw new BadRequestException("Temporary absence transfer not active");
            }
            return lm;
        }).orElseThrow(EntityNotFoundException.withMessage("No movements found"));

        if (!requestForTemporaryAbsenceArrival.getAgencyId().equals(latestExternalMovement.getFromAgency().getId())) {
            throw new BadRequestException("Prisoner is not returning to the same prison");
        }

        offenderBooking.setInOutStatus(IN.name());
        offenderBooking.setLivingUnitMv(null);

        deactivatePreviousMovements(offenderBooking);

        final MovementReason movementReason = getMovementReason(requestForTemporaryAbsenceArrival.getMovementReasonCode(), latestExternalMovement.getMovementReason());

        final LocalDateTime movementTime = getAndCheckMovementTime(requestForTemporaryAbsenceArrival.getDateTime(), offenderBooking.getBookingId());

        final ExternalMovement.ExternalMovementBuilder builder = ExternalMovement.builder()
            .movementDate(movementTime.toLocalDate())
            .movementTime(movementTime)
            .movementType(movementTypeRepository.findById(TAP).orElseThrow(EntityNotFoundException.withMessage(format("No %s movement type found", MovementType.TAP))))
            .movementReason(movementReason)
            .movementDirection(MovementDirection.IN)
            .fromAgency(latestExternalMovement.getToAgency())
            .fromCity(latestExternalMovement.getToCity())
            .toAgency(latestExternalMovement.getFromAgency())
            .active(true)
            .commentText(requestForTemporaryAbsenceArrival.getCommentText());

        if (latestExternalMovement.getEventId() == null) {
            offenderBooking.addExternalMovement(builder.build());
        } else {
            offenderIndividualScheduleRepository
                .findOneByParentEventId(latestExternalMovement.getEventId())
                .ifPresentOrElse(
                    event -> {
                        event.setEventStatus(eventStatusRepository.findById(EventStatus.COMPLETED).orElseThrow());

                        offenderBooking.addExternalMovement(
                            builder
                                .eventId(event.getId())
                                .parentEventId(event.getParentEventId())
                                .build());
                    },
                    () -> offenderBooking.addExternalMovement(builder.build())
                );

        }

        return offenderTransformer.transform(offenderBooking);
    }


    private MovementReason getMovementReason(final String movementReasonCode, final MovementReason defaultMovementReason) {
        if (movementReasonCode == null) return defaultMovementReason;
        return movementReasonRepository.findById(MovementReason.pk(movementReasonCode)).orElseThrow(EntityNotFoundException.withMessage(format("No movement reason %s found", movementReasonCode)));
    }
}
