package uk.gov.justice.hmpps.prison.service;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOut;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToCourt;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToTemporaryAbsence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.City;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EscortAgencyType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementTypeAndReason.Pk;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MovementTypeAndReasonRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderNoPayPeriodRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderPayStatusRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection.IN;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.CRT;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TAP;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TRN;

@Service
@Transactional
@Validated
@Slf4j
@AllArgsConstructor
public class PrisonerTransferService {

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
    private final MovementTypeAndReasonRepository movementTypeAndReasonRepository;
    private final OffenderNoPayPeriodRepository offenderNoPayPeriodRepository;
    private final OffenderPayStatusRepository offenderPayStatusRepository;
    private final ReferenceCodeRepository<City> cityReferenceCodeRepository;

    private final OffenderTransformer offenderTransformer;
    private final EntityManager entityManager;
    private final CourtEventRepository courtEventRepository;
    private final OffenderIndividualScheduleRepository offenderIndividualScheduleRepository;
    private final ReferenceCodeRepository<EventStatus> eventStatusRepository;


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

        createOutMovement(booking, CRT, movementReason, booking.getLocation(), toLocation, null, transferDateTime, requestToTransferOutToCourt.getCommentText(), requestToTransferOutToCourt.getEscortType(), requestToTransferOutToCourt.getCourtEventId());
        Optional.ofNullable(requestToTransferOutToCourt.getCourtEventId()).ifPresent(id -> createScheduleCourtHearingInEvent(markCourtEventComplete(id)));
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

    @VerifyOffenderAccess(overrideRoles = {"TRANSFER_PRISONER_ALPHA"})
    public InmateDetail transferOutPrisonerToTemporaryAbsence(final String prisonerIdentifier, final RequestToTransferOutToTemporaryAbsence requestToTransferOut) {
        // NB This API requires further validation before it can be used for production - it is currently used just to generate test data

        // check that prisoner is active in
        final OffenderBooking booking = getAndCheckOffenderBooking(prisonerIdentifier, false);

        checkMovementTypes(TAP.getCode(), requestToTransferOut.getTransferReasonCode());

        // Generate the external movement out
        final var movementReason = movementReasonRepository.findById(MovementReason.pk(requestToTransferOut.getTransferReasonCode())).orElseThrow(EntityNotFoundException.withMessage(format("No movement reason %s found", requestToTransferOut.getTransferReasonCode())));

        final var transferDateTime = getAndCheckMovementTime(requestToTransferOut.getMovementTime(), booking.getBookingId());
        // set previous active movements to false
        deactivatePreviousMovements(booking);


        Optional.ofNullable(requestToTransferOut.getScheduleEventId())
            .ifPresentOrElse(id -> {
                var individualSchedule = offenderIndividualScheduleRepository.findById(id).orElseThrow(EntityNotFoundException.withMessage(format("No schedule event found for %s", id)));
                createScheduleTAPInEvent(markOffenderIndividualScheduleComplete(individualSchedule));
                createOutMovement(booking, TAP, movementReason, booking.getLocation(), individualSchedule, transferDateTime, requestToTransferOut.getCommentText(), requestToTransferOut.getEscortType());
            }, () -> {
            final var toCity = Optional.ofNullable(requestToTransferOut.getToCity()).map(city -> cityReferenceCodeRepository.findById(City.pk(requestToTransferOut.getToCity())).orElseThrow(EntityNotFoundException.withMessage(format("No city %s found", requestToTransferOut.getToCity())))).orElseThrow(BadRequestException.withMessage("No city specified"));
            createOutMovement(booking, TAP, movementReason, booking.getLocation(), null, toCity, transferDateTime, requestToTransferOut.getCommentText(), requestToTransferOut.getEscortType(), requestToTransferOut.getScheduleEventId());
        });


        if (requestToTransferOut.isShouldReleaseBed()) {
            updateBedAssignmentHistory(booking, transferDateTime);
            booking.setLivingUnitMv(null);
            booking.setAssignedLivingUnit(agencyInternalLocationRepository.findOneByLocationCodeAndAgencyId("TAP", booking.getLocation().getId()).orElseThrow(EntityNotFoundException.withMessage(format("No TAP internal location found for %s", booking.getLocation().getId()))));
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
        booking.setStatusReason(TAP.getCode() + "-" + requestToTransferOut.getTransferReasonCode());
        booking.setCommStatus(null);

        return offenderTransformer.transform(booking);
    }

    private void createScheduleCourtHearingInEvent(CourtEvent parentEvent) {
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
        courtEventRepository.save(returnToPrisonScheduledEvent);
    }

    private void createScheduleTAPInEvent(OffenderIndividualSchedule parentEvent) {
        var returnToPrisonScheduledEvent = OffenderIndividualSchedule
            .builder()
            .eventType(parentEvent.getEventType())
            .eventSubType(parentEvent.getEventSubType())
            .toLocation(parentEvent.getFromLocation())
            .eventStatus(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED).orElseThrow())
            .movementDirection(IN)
            .offenderBooking(parentEvent.getOffenderBooking())
            .parentEventId(parentEvent.getId())
            .eventClass(parentEvent.getEventClass())
            .escortAgencyType(parentEvent.getEscortAgencyType())
            .build();
        offenderIndividualScheduleRepository.save(returnToPrisonScheduledEvent);
    }

    private CourtEvent markCourtEventComplete(Long id) {
        var courtEvent = courtEventRepository.findById(id).orElseThrow(EntityNotFoundException.withMessage(format("No court event found for %s", id)));
        courtEvent.setEventStatus(eventStatusRepository.findById(EventStatus.COMPLETED).orElseThrow());
        return courtEvent;
    }

    private OffenderIndividualSchedule markOffenderIndividualScheduleComplete(OffenderIndividualSchedule individualSchedule) {
        individualSchedule.setEventStatus(eventStatusRepository.findById(EventStatus.COMPLETED).orElseThrow());
        return individualSchedule;
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
                                   final OffenderIndividualSchedule offenderIndividualSchedule,
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
            .toAgency(offenderIndividualSchedule.getToLocation())
            .toAddressId(offenderIndividualSchedule.getToAddressId()) // other types locations could be supported in the future
            .escortText(escortText)
            .escortCode(Optional.ofNullable(offenderIndividualSchedule.getEscortAgencyType()).map(EscortAgencyType::getCode).orElse(null))
            .active(true)
            .commentText(commentText)
            .eventId(offenderIndividualSchedule.getId())
            .build());
    }
    private void createOutMovement(final OffenderBooking booking,
                                   final ReferenceCode.Pk movementCode,
                                   final MovementReason movementReason,
                                   final AgencyLocation fromLocation,
                                   final AgencyLocation toLocation,
                                   final City toCity,
                                   final LocalDateTime movementTime,
                                   final String commentText,
                                   final String escortText,
                                   final Long eventId) {
        booking.addExternalMovement(ExternalMovement.builder()
            .movementDate(movementTime.toLocalDate())
            .movementTime(movementTime)
            .movementType(movementTypeRepository.findById(movementCode).orElseThrow(EntityNotFoundException.withMessage(format("No %s movement type found", movementCode))))
            .movementReason(movementReason)
            .movementDirection(MovementDirection.OUT)
            .reportingDate(movementTime.toLocalDate())
            .fromAgency(fromLocation)
            .toAgency(toLocation)
            .toCity(toCity)
            .escortText(escortText)
            .active(true)
            .commentText(commentText)
            .eventId(eventId)
            .build());
    }

    private void checkMovementTypes(final String movementCode, final String reasonCode) {
        final var movementTypeAndReason = Pk.builder().type(movementCode).reasonCode(reasonCode).build();

        movementTypeAndReasonRepository.findById(movementTypeAndReason)
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

                if (!now.isBefore(p.getStartDate()) && now.isBefore(p.getEndDate() != null ? p.getEndDate() : now)) {
                    p.setEndDate(p.getStartDate().isBefore(movementDate) ? movementDate : p.getStartDate());
                }

                if (p.getStartDate().isAfter(now)) {
                    p.setEndDate(p.getStartDate());
                }
            });
    }


}
