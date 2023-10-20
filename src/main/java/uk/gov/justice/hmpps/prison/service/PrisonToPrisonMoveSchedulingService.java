package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.SchedulePrisonToPrisonMove;
import uk.gov.justice.hmpps.prison.api.model.ScheduledPrisonToPrisonMove;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EscortAgencyType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule;
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransferCancellationReason;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus.COMPLETED;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus.SCHEDULED_APPROVED;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection.OUT;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule.EventClass.EXT_MOV;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class PrisonToPrisonMoveSchedulingService {

    private static final String PRISON_TRANSFER = "TRN";

    private static final String NORMAL_TRANSFER = "NOTR";

    private final Clock clock;

    private final OffenderBookingRepository offenderBookingRepository;

    private final AgencyLocationRepository agencyLocationRepository;

    private final ReferenceCodeRepository<EscortAgencyType> escortAgencyTypeRepository;

    private final ReferenceCodeRepository<EventStatus> eventStatusRepository;

    private final ReferenceCodeRepository<TransferCancellationReason> transferCancellationReasonRepository;

    private final OffenderIndividualScheduleRepository scheduleRepository;

    public PrisonToPrisonMoveSchedulingService(final Clock clock,
                                               final OffenderBookingRepository offenderBookingRepository,
                                               final AgencyLocationRepository agencyLocationRepository,
                                               final ReferenceCodeRepository<EscortAgencyType> escortAgencyTypeRepository,
                                               final ReferenceCodeRepository<EventStatus> eventStatusRepository,
                                               final ReferenceCodeRepository<TransferCancellationReason> transferCancellationReasonRepository,
                                               final OffenderIndividualScheduleRepository scheduleRepository) {
        this.clock = clock;
        this.offenderBookingRepository = offenderBookingRepository;
        this.agencyLocationRepository = agencyLocationRepository;
        this.escortAgencyTypeRepository = escortAgencyTypeRepository;
        this.eventStatusRepository = eventStatusRepository;
        this.scheduleRepository = scheduleRepository;
        this.transferCancellationReasonRepository = transferCancellationReasonRepository;
    }

    @Transactional
    public ScheduledPrisonToPrisonMove schedule(final Long bookingId, final SchedulePrisonToPrisonMove move) {
        log.debug("Scheduling a prison to prison move for booking: {} with details: {}", bookingId, move);

        checkIsInFuture(move.getScheduledMoveDateTime());
        checkNotTheSame(move.getFromPrisonLocation(), move.getToPrisonLocation());

        final var activeBooking = activeOffenderBookingFor(bookingId);

        checkFromLocationMatchesThe(activeBooking, move.getFromPrisonLocation());

        final var escortAgencyType = getEscortAgencyType(move.getEscortType());

        final var scheduledMove = scheduleMove(activeBooking, getActive(move.getToPrisonLocation()), escortAgencyType, move.getScheduledMoveDateTime());

        log.debug("Prison to prison move scheduled with event id: {} for offender: {}, move details: {}",
                scheduledMove.getId(), activeBooking.getOffender().getNomsId(), move);

        return ScheduledPrisonToPrisonMove.builder()
                .id(scheduledMove.getId())
                .scheduledMoveDateTime(scheduledMove.getEventDateTime())
                .fromPrisonLocation(AgencyTransformer.transform(scheduledMove.getFromLocation(), false))
                .toPrisonLocation(AgencyTransformer.transform(scheduledMove.getToLocation(), false))
                .build();
    }

    private void checkIsInFuture(final LocalDateTime datetime) {
        checkArgument(datetime.isAfter(LocalDateTime.now(clock)), "Prison to prison move must be in the future.");
    }

    private OffenderBooking activeOffenderBookingFor(final Long bookingId) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));

        checkIsActive(offenderBooking);

        return offenderBooking;
    }

    private void checkFromLocationMatchesThe(final OffenderBooking booking, final String from) {
        checkArgument(booking.getLocation().getId().equals(from), "Prison to prison move from prison does not match that of the booking.");
    }

    private void checkNotTheSame(final String from, final String to) {
        checkArgument(!from.equals(to), "Prison to prison move from and to prisons cannot be the same.");
    }

    private AgencyLocation getActive(final String prison) {
        final var agency = agencyLocationRepository.findById(prison).orElseThrow(() -> EntityNotFoundException.withMessage("Prison with id %s not found.", prison));

        checkArgument(agency.isActive(), "Prison with id %s not active.", prison);

        checkArgument(agency.getType().getCode().equals("INST"), "Prison to prison move to prison is not a prison.");

        return agency;
    }

    private EscortAgencyType getEscortAgencyType(final String key) {
        return escortAgencyTypeRepository.findById(EscortAgencyType.pk(key))
                .orElseThrow((() -> EntityNotFoundException.withMessage("Escort type %s for prison to prison move not found.", key)));
    }

    private OffenderIndividualSchedule scheduleMove(final OffenderBooking booking,
                                                    final AgencyLocation toPrison,
                                                    final EscortAgencyType escortAgencyType,
                                                    final LocalDateTime moveDateTime) {
        return scheduleRepository.save(OffenderIndividualSchedule.builder()
                .eventDate(moveDateTime.toLocalDate())
                .startTime(moveDateTime)
                .eventClass(EXT_MOV)
                .eventType(PRISON_TRANSFER)
                .eventSubType(NORMAL_TRANSFER)
                .eventStatus(eventStatusRepository.findById(SCHEDULED_APPROVED).orElseThrow())
                .escortAgencyType(escortAgencyType)
                .fromLocation(booking.getLocation())
                .toLocation(toPrison)
                .movementDirection(OUT)
                .offenderBooking(booking)
                .build());
    }

    @Transactional
    public void cancel(final Long bookingId, final Long scheduledMoveId, final String transferCancellationReasonCode) {
        final var move = scheduleRepository.findById(scheduledMoveId).orElseThrow(() -> EntityNotFoundException.withMessage("Scheduled prison move with id %s not found.", scheduledMoveId));

        checkIsPrison(move);
        checkIsAssociated(bookingId, move);
        checkIsActive(move.getOffenderBooking());

        final var cancelled = eventStatusRepository.findById(EventStatus.CANCELLED).orElseThrow(() -> EntityNotFoundException.withMessage("Event status cancelled not found."));

        if (is(move, cancelled)) {
            return;
        }

        checkCanCancel(move);

        move.setEventStatus(cancelled);
        move.setCancellationReason(transferCancellationReasonRepository.findById(TransferCancellationReason.pk(transferCancellationReasonCode)).orElseThrow(() -> EntityNotFoundException.withMessage("Cancellation reason %s not found.", transferCancellationReasonCode)));

        scheduleRepository.save(move);

        log.debug("Cancelled scheduled prison to prison move with id {} for offender {}", move.getId(), move.getOffenderBooking().getOffender().getNomsId());
    }

    private boolean is(final OffenderIndividualSchedule move, final EventStatus status) {
        return move.getEventStatus().equals(status);
    }

    private void checkIsActive(final OffenderBooking booking) {
        checkArgument(booking.isActive(), "Offender booking for prison to prison move with id %s is not active.", booking.getBookingId());
    }

    private void checkIsPrison(final OffenderIndividualSchedule scheduledMove) {
        checkArgument(PRISON_TRANSFER.equals(scheduledMove.getEventType()), "Scheduled move with id %s not a prison move.", scheduledMove.getId());
    }

    private void checkCanCancel(final OffenderIndividualSchedule scheduledMove) {
        checkArgument(scheduledMove.getEventStatus().equals(eventStatusRepository.findById(SCHEDULED_APPROVED).orElseThrow()), "Move with id %s is not in a scheduled state.", scheduledMove.getId());
    }

    private void checkIsAssociated(final Long bookingId, final OffenderIndividualSchedule scheduledMove) {
        checkArgument(scheduledMove.getOffenderBooking().getBookingId().equals(bookingId), "Booking with id %s not associated with the supplied move id %s.", bookingId, scheduledMove.getId());
    }

    public Optional<OffenderIndividualSchedule> completeScheduledChildHearingEvent(@Nullable Long bookingId, long parentEventId) {
        return scheduleRepository.findOneByOffenderBookingBookingIdAndParentEventId(bookingId, parentEventId)
            .map(scheduleEvent -> {
                scheduleEvent.setEventStatus(eventStatusRepository.findById(COMPLETED).orElseThrow());
                return scheduleEvent;
            });
    }
}
