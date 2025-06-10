package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.CourtEvent;
import uk.gov.justice.hmpps.prison.api.model.CourtEventBasic;
import uk.gov.justice.hmpps.prison.api.model.CreateExternalMovement;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.MovementCount;
import uk.gov.justice.hmpps.prison.api.model.MovementSummary;
import uk.gov.justice.hmpps.prison.api.model.BookingMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderLatestArrivalDate;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderOut;
import uk.gov.justice.hmpps.prison.api.model.OffenderOutTodayDto;
import uk.gov.justice.hmpps.prison.api.model.OutOnTemporaryAbsenceSummary;
import uk.gov.justice.hmpps.prison.api.model.PrisonerInPrisonSummary;
import uk.gov.justice.hmpps.prison.api.model.ReleaseEvent;
import uk.gov.justice.hmpps.prison.api.model.TransferEvent;
import uk.gov.justice.hmpps.prison.api.model.TransferSummary;
import uk.gov.justice.hmpps.prison.repository.MovementsRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.City;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MovementTypeAndReasonRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.apache.commons.lang3.StringUtils.upperCase;
import static org.apache.commons.text.WordUtils.capitalizeFully;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
public class MovementsService {

    private final MovementsRepository movementsRepository;
    private final ExternalMovementRepository externalMovementRepository;
    private final CourtEventRepository courtEventRepository;
    private final AgencyLocationRepository agencyLocationRepository;
    private final ReferenceCodeRepository<MovementType> movementTypeRepository;
    private final ReferenceCodeRepository<MovementReason> movementReasonRepository;
    private final OffenderBookingRepository offenderBookingRepository;
    private final MovementTypeAndReasonRepository movementTypeAndReasonRepository;
    private final int maxBatchSize;


    public MovementsService(final MovementsRepository movementsRepository,
                            final ExternalMovementRepository externalMovementRepository,
                            final CourtEventRepository courtEventRepository,
                            final AgencyLocationRepository agencyLocationRepository,
                            final ReferenceCodeRepository<MovementType> movementTypeRepository,
                            final ReferenceCodeRepository<MovementReason> movementReasonRepository,
                            final OffenderBookingRepository offenderBookingRepository,
                            final MovementTypeAndReasonRepository movementTypeAndReasonRepository,
                            @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.movementsRepository = movementsRepository;
        this.externalMovementRepository = externalMovementRepository;
        this.courtEventRepository = courtEventRepository;
        this.offenderBookingRepository = offenderBookingRepository;
        this.agencyLocationRepository = agencyLocationRepository;
        this.movementTypeRepository = movementTypeRepository;
        this.movementReasonRepository = movementReasonRepository;
        this.movementTypeAndReasonRepository = movementTypeAndReasonRepository;
        this.maxBatchSize = maxBatchSize;
    }

    public List<Movement> getRecentMovementsByDate(final LocalDateTime fromDateTime, final LocalDate movementDate, final List<String> movementTypes) {
        return movementsRepository.getRecentMovementsByDate(fromDateTime, movementDate, movementTypes);
    }

    public PrisonerInPrisonSummary getPrisonerInPrisonSummary(final String offenderNo) {
        final var latestBooking = offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, 1).orElseThrow(EntityNotFoundException.withId(offenderNo));

        return latestBooking.getOffender().getRootOffender().getPrisonerInPrisonSummary();
    }

    public List<Movement> getMovementsByOffenders(final List<String> offenderNumbers, final List<String> movementTypes, final boolean latestOnly, final boolean allBookings) {
        final var movements = Lists.partition(offenderNumbers, maxBatchSize)
            .stream()
            .map(offenders -> movementsRepository.getMovementsByOffenders(offenders, movementTypes, latestOnly, allBookings))
            .flatMap(List::stream);


        return movements
            .map(this::mapMovementDescriptions)
            .collect(toList());
    }

    public List<Movement> getMovementsByOffender(final String offenderNumber, final List<String> movementTypes, final boolean allBookings, final LocalDate movementsAfter) {
        final var movements = movementsRepository.getMovementsByOffender(offenderNumber, movementTypes, allBookings, movementsAfter);

        return movements.stream()
            .map(this::mapMovementDescriptions)
            .collect(toList());
    }

    public List<BookingMovement> getMovementsByBooking(final Long bookingId) {
        final var movements = externalMovementRepository.findAllByOffenderBooking_BookingId(bookingId);

        return movements.stream()
            .map(m -> new BookingMovement(
                    m.getMovementSequence().intValue(),
                    m.getFromAgency() == null ? null : m.getFromAgency().getId(),
                    m.getToAgency() == null ? null : m.getToAgency().getId(),
                    m.getMovementType() == null ? null : m.getMovementType().getCode(),
                    m.getMovementDirection() == null ? null : m.getMovementDirection().name(),
                    m.getMovementTime(),
                    m.getMovementReasonCode()
                )
            )
            .collect(toList());
    }

    private Movement mapMovementDescriptions(final Movement movement) {
        return movement.toBuilder()
            .fromAgencyDescription(StringUtils.trimToEmpty(LocationProcessor.formatLocation(movement.getFromAgencyDescription())))
            .toAgencyDescription(StringUtils.trimToEmpty(LocationProcessor.formatLocation(movement.getToAgencyDescription())))
            .toCity(capitalizeFully(StringUtils.trimToEmpty(movement.getToCity())))
            .fromCity(capitalizeFully(StringUtils.trimToEmpty(movement.getFromCity())))
            .build();
    }

    public MovementCount getMovementCount(final String agencyId, final LocalDate date) {
        return movementsRepository.getMovementCount(agencyId, date == null ? LocalDate.now() : date);
    }

    public List<OffenderOutTodayDto> getOffendersOut(final String agencyId, final LocalDate movementDate, final String movementType) {

        final var offenders = movementsRepository.getOffendersOut(agencyId, movementDate, upperCase(stripToNull(movementType)));

        return offenders
            .stream()
            .map(this::toOffenderOutTodayDto)
            .collect(toList());
    }

    private OffenderOutTodayDto toOffenderOutTodayDto(final OffenderMovement offenderMovement) {
        return OffenderOutTodayDto
            .builder()
            .dateOfBirth(offenderMovement.getDateOfBirth())
            .firstName(capitalizeFully(offenderMovement.getFirstName()))
            .lastName(capitalizeFully(offenderMovement.getLastName()))
            .reasonDescription(capitalizeFully(offenderMovement.getMovementReasonDescription()))
            .offenderNo(offenderMovement.getOffenderNo())
            .timeOut(offenderMovement.getMovementTime())
            .movementType(offenderMovement.getMovementType())
            .toAddress(offenderMovement.getToAddress())
            .build();
    }

    public List<OffenderMovement> getEnRouteOffenderMovements(final String agencyId, final LocalDate date) {

        final var movements = movementsRepository.getEnrouteMovementsOffenderMovementList(agencyId, date);

        return movements.stream().map(movement -> movement.toBuilder()
                .fromAgencyDescription(LocationProcessor.formatLocation(movement.getFromAgencyDescription()))
                .toAgencyDescription(LocationProcessor.formatLocation(movement.getToAgencyDescription()))
                .build())
            .collect(toList());

    }

    public int getEnRouteOffenderCount(final String agencyId, final LocalDate date) {
        final var defaultedDate = date == null ? LocalDate.now() : date;
        return movementsRepository.getEnRouteMovementsOffenderCount(agencyId, defaultedDate);
    }

    public List<OffenderIn> getOffendersIn(final String agencyId, final LocalDate date) {
        return movementsRepository.getOffendersIn(agencyId, date);
    }

    public List<OffenderInReception> getOffendersInReception(final String agencyId) {
        return movementsRepository.getOffendersInReception(agencyId)
            .stream()
            .map(offender -> offender.toBuilder()
                .firstName(capitalizeFully(offender.getFirstName()))
                .lastName(capitalizeFully(offender.getLastName()))
                .build())
            .collect(toList());
    }

    public List<OffenderOut> getOffendersCurrentlyOut(final long livingUnitId) {
        return movementsRepository
            .getOffendersCurrentlyOut(livingUnitId)
            .stream()
            .map(offender -> offender.toBuilder()
                .firstName(capitalizeFully(offender.getFirstName()))
                .lastName(capitalizeFully(offender.getLastName()))
                .build())
            .collect(toList());
    }

    public List<OffenderOut> getOffendersCurrentlyOut(final String agencyId) {
        return movementsRepository
            .getOffendersCurrentlyOut(agencyId)
            .stream()
            .map(offender -> offender.toBuilder()
                .firstName(capitalizeFully(offender.getFirstName()))
                .lastName(capitalizeFully(offender.getLastName()))
                .build())
            .collect(toList());
    }

    public TransferSummary getTransferMovementsForAgencies(final List<String> agencyIds,
                                                           final LocalDateTime fromDateTime, final LocalDateTime toDateTime,
                                                           final boolean courtEvents, final boolean releaseEvents, final boolean transferEvents, final boolean movements) {

        checkTransferParametersAndThrowIfIncorrect(agencyIds, fromDateTime, toDateTime, courtEvents, releaseEvents, transferEvents, movements);

        final List<CourtEvent> listOfCourtEvents = courtEvents ?
            movementsRepository.getCourtEvents(agencyIds, fromDateTime, toDateTime).stream()
                .map(event -> event.toBuilder()
                    .fromAgencyDescription(LocationProcessor.formatLocation(event.getFromAgencyDescription()))
                    .toAgencyDescription(LocationProcessor.formatLocation(event.getToAgencyDescription()))
                    .build())
                .collect(toList()) :
            List.of();

        final List<ReleaseEvent> listOfReleaseEvents = releaseEvents ?
            movementsRepository.getOffenderReleases(agencyIds, fromDateTime, toDateTime).stream()
                .map(event -> event.toBuilder()
                    .fromAgencyDescription(LocationProcessor.formatLocation(event.getFromAgencyDescription()))
                    .build())
                .collect(toList()) :
            List.of();

        final List<TransferEvent> listOfTransferEvents = transferEvents ?
            getTransferEvents(agencyIds, fromDateTime, toDateTime) :
            List.of();

        final List<MovementSummary> listOfMovements = movements ?
            movementsRepository.getCompletedMovementsForAgencies(agencyIds, fromDateTime, toDateTime).stream()
                .map(event -> event.toBuilder()
                    .fromAgencyDescription(LocationProcessor.formatLocation(event.getFromAgencyDescription()))
                    .toAgencyDescription(LocationProcessor.formatLocation(event.getToAgencyDescription()))
                    .build())
                .collect(toList()) :
            List.of();

        return TransferSummary.builder()
            .courtEvents(listOfCourtEvents)
            .releaseEvents(listOfReleaseEvents)
            .transferEvents(listOfTransferEvents)
            .movements(listOfMovements)
            .build();
    }

    private void checkTransferParametersAndThrowIfIncorrect(final List<String> agencyIds, final LocalDateTime fromDateTime, final LocalDateTime toDateTime,
                                                            final boolean courtEvents, final boolean releaseEvents, final boolean transferEvents,
                                                            final boolean movements) {
        // Needs at least one agency ID specified
        if (CollectionUtils.isEmpty(agencyIds))
            logErrorAndThrowBadRequest("No agency location identifiers were supplied");

        // The from time must be before the to time
        if (fromDateTime.isAfter(toDateTime))
            logErrorAndThrowBadRequest("The supplied fromDateTime parameter is after the toDateTime value");

        // The time period requested must be shorter than or equal to 24 hours
        if (toDateTime.isAfter(fromDateTime.plusHours(24)))
            logErrorAndThrowBadRequest("The supplied time period is more than 24 hours - limit to 24 hours maximum");

        // One of the event/movement type query parameters must be true
        if (!courtEvents && !releaseEvents && !transferEvents && !movements)
            logErrorAndThrowBadRequest("At least one query parameter must be true [courtEvents|releaseEvents|transferEvents|movements]");
    }

    private void logErrorAndThrowBadRequest(final String errorMessage) {
        log.info("Request parameters supplied were not valid - {}", errorMessage);
        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorMessage);
    }

    private List<TransferEvent> getTransferEvents(final List<String> agencyIds, final LocalDateTime fromDateTime, final LocalDateTime toDateTime) {
        final var datesToTry = splitDatesIfTheySpanAcrossDifferentDays(fromDateTime, toDateTime);

        return datesToTry.stream()
            .flatMap(date -> movementsRepository.getIndividualSchedules(agencyIds, date).stream())
            .filter(isTransferAndNotDeleted())
            .filter(isStartTimeBetween(fromDateTime, toDateTime))
            .map(scheduled -> scheduled.toBuilder()
                .fromAgencyDescription(LocationProcessor.formatLocation(scheduled.getFromAgencyDescription()))
                .toAgencyDescription(LocationProcessor.formatLocation(scheduled.getToAgencyDescription()))
                .build())
            .collect(toList());
    }

    private Predicate<TransferEvent> isTransferAndNotDeleted() {
        return scheduledEvent -> scheduledEvent.getEventClass().equals("EXT_MOV") && !scheduledEvent.getEventStatus().equals("DEL");
    }

    private Predicate<TransferEvent> isStartTimeBetween(final LocalDateTime fromDateTime, final LocalDateTime toDateTime) {
        return scheduledEvent -> {
            final var startTime = scheduledEvent.getStartTime();
            return startTime != null && (startTime.isEqual(fromDateTime) || startTime.isAfter(fromDateTime) &&
                (startTime.isEqual(toDateTime) || startTime.isBefore(toDateTime)));
        };
    }

    private List<LocalDate> splitDatesIfTheySpanAcrossDifferentDays(final LocalDateTime fromDateTime, final LocalDateTime toDateTime) {
        if (toDateTime.toLocalDate().isAfter(fromDateTime.toLocalDate()))
            return List.of(fromDateTime.toLocalDate(), toDateTime.toLocalDate());

        return List.of(fromDateTime.toLocalDate());
    }

    public List<CourtEventBasic> getUpcomingCourtAppearances() {
        return courtEventRepository.getCourtEventsUpcoming(LocalDate.now().atStartOfDay())
            .stream()
            .map(e -> CourtEventBasic.builder()
                .offenderNo((String) e.get("offenderNo"))
                .startTime((LocalDateTime) e.get("startTime"))
                .court((String) e.get("court"))
                .courtDescription(LocationProcessor.formatLocation((String) e.get("courtDescription")))
                .eventSubType((String) e.get("eventSubType"))
                .eventDescription((String) e.get("eventDescription"))
                .hold("Y".equals(e.get("holdFlag")))
                .build()
            ).toList();
    }

    @Transactional
    public OffenderMovement createExternalMovement(@NotNull final Long bookingId, final CreateExternalMovement createExternalMovement) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId)
            .orElseThrow(EntityNotFoundException.withMessage("booking not found using %s", bookingId));

        final var movementType = movementTypeRepository.findById(MovementType.pk(createExternalMovement.getMovementType()))
            .orElseThrow(EntityNotFoundException.withMessage("movementType not found using: %s", createExternalMovement.getMovementType()));

        final var movementReason = movementReasonRepository.findById(MovementReason.pk(createExternalMovement.getMovementReason()))
            .orElseThrow(EntityNotFoundException.withMessage("movementReason not found using: %s", createExternalMovement.getMovementReason()));

        final var movementReasons =
            movementTypeAndReasonRepository.findMovementTypeAndReasonByTypeIs(createExternalMovement.getMovementType());

        if (movementReasons.stream().noneMatch(r -> r.getReasonCode().equals(createExternalMovement.getMovementReason())))
            throw new EntityNotFoundException("Invalid movement reason for supplied movement type");

        if (offenderBooking.isActive())
            throw new IllegalStateException("You can only create an external movement for inactive offenders");

        final var fromAgency = agencyLocationRepository.findById(createExternalMovement.getFromAgencyId())
            .orElseThrow(EntityNotFoundException.withMessage("fromAgency not found using: %s", createExternalMovement.getFromAgencyId()));

        final var toAgency = agencyLocationRepository.findById(createExternalMovement.getToAgencyId())
            .orElseThrow(EntityNotFoundException.withMessage("toAgency not found using: %s", createExternalMovement.getToAgencyId()));

        final var externalMovement = ExternalMovement
            .builder()
            .offenderBooking(offenderBooking)
            .movementSequence(0L)
            .movementDate(createExternalMovement.getMovementTime().toLocalDate())
            .movementTime(createExternalMovement.getMovementTime())
            .fromAgency(fromAgency)
            .toAgency(toAgency)
            .movementDirection(createExternalMovement.getDirectionCode())
            .movementType(movementType)
            .movementReason(movementReason)
            .build();

        offenderBooking.addExternalMovement(externalMovement);

        return transformToOffenderMovement(externalMovement);
    }

    private static OffenderMovement transformToOffenderMovement(final ExternalMovement externalMovement) {
        return OffenderMovement
            .builder()
            .offenderNo(externalMovement.getOffenderBooking().getOffender().getNomsId())
            .bookingId(externalMovement.getOffenderBooking().getBookingId())
            .dateOfBirth(externalMovement.getOffenderBooking().getOffender().getBirthDate())
            .firstName(externalMovement.getOffenderBooking().getOffender().getFirstName())
            .lastName(externalMovement.getOffenderBooking().getOffender().getLastName())
            .middleName(externalMovement.getOffenderBooking().getOffender().getMiddleName())
            .movementDate(externalMovement.getMovementDate())
            .movementTime(externalMovement.getMovementTime().toLocalTime())
            .directionCode(externalMovement.getMovementDirection().toString())
            .movementReason(externalMovement.getMovementReason().getCode())
            .movementReasonDescription(externalMovement.getMovementReason().getDescription())
            .movementType(externalMovement.getMovementType().getCode())
            .movementTypeDescription(externalMovement.getMovementType().getDescription())
            .fromAgency(externalMovement.getFromAgency().getId())
            .fromAgencyDescription(LocationProcessor.formatLocation(externalMovement.getFromAgency().getDescription()))
            .toAgency(externalMovement.getToAgency().getId())
            .toAgencyDescription(LocationProcessor.formatLocation(externalMovement.getToAgency().getDescription()))
            .build();
    }

    public Page<OffenderIn> getOffendersIn(final String agencyId, final LocalDateTime fromDate, final LocalDateTime toDate, final Pageable pageable, final boolean allMovements) {
        final var page = allMovements
            ? externalMovementRepository.findAllMovements(agencyId, MovementDirection.IN, fromDate, toDate, pageable)
            : externalMovementRepository.findMovements(agencyId, true, MovementDirection.IN, fromDate, toDate, pageable);
        final var movements = page.getContent().stream().map(this::transform).collect(toList());
        return new PageImpl<>(movements, pageable, page.getTotalElements());
    }

    public List<OutOnTemporaryAbsenceSummary> getOffendersOutOnTemporaryAbsence(final String agencyId) {
        return
            externalMovementRepository.findCurrentTemporaryAbsencesForPrison(
                    agencyId,
                    movementTypeRepository.findById(MovementType.TAP).orElseThrow())
                .stream().map(MovementsService::transformToOutOnTemporaryAbsenceSummary).collect(toList());
    }

    private static OutOnTemporaryAbsenceSummary transformToOutOnTemporaryAbsenceSummary(final ExternalMovement movement) {
        final Offender offender = movement.getOffenderBooking().getOffender();
        final AgencyLocation toAgency = movement.getToAgency();
        final City toCity = movement.getToCity();

        return OutOnTemporaryAbsenceSummary
            .builder()
            .offenderNo(offender.getNomsId())
            .firstName(offender.getFirstName())
            .lastName(offender.getLastName())
            .dateOfBirth(offender.getBirthDate())
            .movementTime(movement.getMovementTime())
            .toAgency(toAgency == null ? null : toAgency.getId())
            .toAgencyDescription(toAgency == null ? null : toAgency.getDescription())
            .toCity(toCity == null ? null : toCity.getDescription())
            .movementReason(movement.getMovementReason().getDescription())
            .movementReasonCode(movement.getMovementReason().getCode())
            .commentText(movement.getCommentText())
            .build();
    }

    private OffenderIn transform(ExternalMovement m) {
        final var booking = m.getOffenderBooking();
        final var offender = booking.getOffender();
        final var description = Optional.ofNullable(booking.getAssignedLivingUnit()).map(unit -> firstNonNull(unit.getUserDescription(), unit.getDescription())).orElse(null);
        final var fromAgency = Optional.ofNullable(m.getFromAgency());
        final var toAgency = Optional.ofNullable(m.getToAgency());
        final var fromCityDescription = Optional.ofNullable(m.getFromCity()).map(City::getDescription).orElse(null);
        final var toCityDescription = Optional.ofNullable(m.getToCity()).map(City::getDescription).orElse(null);
        return new OffenderIn(
            offender.getNomsId(),
            m.getOffenderBooking().getBookingId(),
            offender.getBirthDate(),
            capitalizeFully(offender.getFirstName()),
            capitalizeFully(offender.getMiddleName()),
            capitalizeFully(offender.getLastName()),
            fromAgency.map(AgencyLocation::getId).orElse(null),
            fromAgency.map(AgencyLocation::getDescription).orElse(null),
            toAgency.map(AgencyLocation::getId).orElse(null),
            toAgency.map(AgencyLocation::getDescription).orElse(null),
            fromCityDescription,
            toCityDescription,
            m.getMovementTime().toLocalTime(),
            m.getMovementTime(),
            description,
            m.getMovementType() != null ? m.getMovementType().getCode() : "",
            m.getMovementReason() != null ? m.getMovementReason().getDescription() : "",
            null);
    }

    public Optional<LocalDate> getLatestArrivalDate(final String offenderNumber) {
        return movementsRepository.getLatestArrivalDate(offenderNumber);
    }

    public List<OffenderLatestArrivalDate> getLatestArrivalDates(final List<String> offenderNumbers) {
        return Lists.partition(offenderNumbers, maxBatchSize)
            .stream()
            .map(movementsRepository::getLatestArrivalDates)
            .flatMap(List::stream)
            .sorted(comparing(OffenderLatestArrivalDate::getOffenderNo))
            .toList();
    }
}
