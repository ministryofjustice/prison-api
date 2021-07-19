package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.CourtEvent;
import uk.gov.justice.hmpps.prison.api.model.CourtEventBasic;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.MovementCount;
import uk.gov.justice.hmpps.prison.api.model.MovementSummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderOut;
import uk.gov.justice.hmpps.prison.api.model.OffenderOutTodayDto;
import uk.gov.justice.hmpps.prison.api.model.PrisonerInPrisonSummary;
import uk.gov.justice.hmpps.prison.api.model.ReleaseEvent;
import uk.gov.justice.hmpps.prison.api.model.RollCount;
import uk.gov.justice.hmpps.prison.api.model.TransferEvent;
import uk.gov.justice.hmpps.prison.api.model.TransferSummary;
import uk.gov.justice.hmpps.prison.repository.MovementsRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.City;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.firstNonNull;
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
    private final OffenderBookingRepository offenderBookingRepository;
    private final int maxBatchSize;


    public MovementsService(final MovementsRepository movementsRepository,
                            final ExternalMovementRepository externalMovementRepository,
                            final CourtEventRepository courtEventRepository,
                            final OffenderBookingRepository offenderBookingRepository,
                            @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.movementsRepository = movementsRepository;
        this.externalMovementRepository = externalMovementRepository;
        this.courtEventRepository = courtEventRepository;
        this.offenderBookingRepository = offenderBookingRepository;
        this.maxBatchSize = maxBatchSize;
    }

    @PreAuthorize("hasAnyRole('SYSTEM_USER','GLOBAL_SEARCH')")
    public List<Movement> getRecentMovementsByDate(final LocalDateTime fromDateTime, final LocalDate movementDate, final List<String> movementTypes) {
        return movementsRepository.getRecentMovementsByDate(fromDateTime, movementDate, movementTypes);
    }

    @VerifyBookingAccess
    public Optional<Movement> getMovementByBookingIdAndSequence(@NotNull final Long bookingId, @NotNull final Integer sequenceNumber) {
        return movementsRepository.getMovementByBookingIdAndSequence(bookingId, sequenceNumber)
            .map(movement -> movement.toBuilder()
                .fromAgencyDescription(StringUtils.trimToEmpty(LocationProcessor.formatLocation(movement.getFromAgencyDescription())))
                .toAgencyDescription(StringUtils.trimToEmpty(LocationProcessor.formatLocation(movement.getToAgencyDescription())))
                .toCity(capitalizeFully(StringUtils.trimToEmpty(movement.getToCity())))
                .fromCity(capitalizeFully(StringUtils.trimToEmpty(movement.getFromCity())))
                .build());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'VIEW_PRISONER_DATA')")
    public PrisonerInPrisonSummary getPrisonerInPrisonSummary(final String offenderNo) {
        final var latestBooking = offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, 1).orElseThrow(EntityNotFoundException.withId(offenderNo));

        return latestBooking.getOffender().getRootOffender().getPrisonerInPrisonSummary();
    }



    @PreAuthorize("hasAnyRole('SYSTEM_USER','GLOBAL_SEARCH', 'VIEW_PRISONER_DATA')")
    public List<Movement> getMovementsByOffenders(final List<String> offenderNumbers, final List<String> movementTypes, final boolean latestOnly, final boolean allBookings) {
        final var movements = Lists.partition(offenderNumbers, maxBatchSize)
            .stream()
            .map(offenders -> movementsRepository.getMovementsByOffenders(offenders, movementTypes, latestOnly, allBookings))
            .flatMap(List::stream);

        return movements.map(movement -> movement.toBuilder()
            .fromAgencyDescription(StringUtils.trimToEmpty(LocationProcessor.formatLocation(movement.getFromAgencyDescription())))
            .toAgencyDescription(StringUtils.trimToEmpty(LocationProcessor.formatLocation(movement.getToAgencyDescription())))
            .toCity(capitalizeFully(StringUtils.trimToEmpty(movement.getToCity())))
            .fromCity(capitalizeFully(StringUtils.trimToEmpty(movement.getFromCity())))
            .build())
            .collect(toList());
    }

    @VerifyAgencyAccess
    public List<RollCount> getRollCount(final String agencyId, final boolean unassigned) {
        return movementsRepository.getRollCount(agencyId, unassigned ? "N" : "Y");
    }

    @VerifyAgencyAccess
    public MovementCount getMovementCount(final String agencyId, final LocalDate date) {
        return movementsRepository.getMovementCount(agencyId, date == null ? LocalDate.now() : date);
    }

    @VerifyAgencyAccess
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
            .build();
    }

    @VerifyAgencyAccess
    public List<OffenderMovement> getEnrouteOffenderMovements(final String agencyId, final LocalDate date) {

        final var movements = movementsRepository.getEnrouteMovementsOffenderMovementList(agencyId, date);

        return movements.stream().map(movement -> movement.toBuilder()
            .fromAgencyDescription(LocationProcessor.formatLocation(movement.getFromAgencyDescription()))
            .toAgencyDescription(LocationProcessor.formatLocation(movement.getToAgencyDescription()))
            .build())
            .collect(toList());

    }

    public int getEnrouteOffenderCount(final String agencyId, final LocalDate date) {
        final var defaultedDate = date == null ? LocalDate.now() : date;
        return movementsRepository.getEnrouteMovementsOffenderCount(agencyId, defaultedDate);
    }

    @VerifyAgencyAccess
    public List<OffenderIn> getOffendersIn(final String agencyId, final LocalDate date) {
        final var offendersIn = movementsRepository.getOffendersIn(agencyId, date);

        return offendersIn
            .stream()
            .map(offender -> offender.toBuilder()
                .firstName(capitalizeFully(offender.getFirstName()))
                .lastName(capitalizeFully(offender.getLastName()))
                .middleName(capitalizeFully(StringUtils.trimToEmpty(offender.getMiddleName())))
                .fromAgencyDescription(LocationProcessor.formatLocation(offender.getFromAgencyDescription()))
                .toAgencyDescription(LocationProcessor.formatLocation(offender.getToAgencyDescription()))
                .location(StringUtils.trimToEmpty(offender.getLocation()))
                .movementTime(offender.getMovementDateTime().toLocalTime())
                .build())
            .collect(toList());
    }

    @VerifyAgencyAccess
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

    @PreAuthorize("hasAnyRole('SYSTEM_USER','GLOBAL_SEARCH')")
    public TransferSummary getTransferMovementsForAgencies(final List<String> agencyIds,
                                                           final LocalDateTime fromDateTime, final LocalDateTime toDateTime,
                                                           final boolean courtEvents, final boolean releaseEvents, final boolean transferEvents, final boolean movements) {

        final var badRequestMsg = checkTransferParameters(agencyIds, fromDateTime, toDateTime, courtEvents, releaseEvents, transferEvents, movements);
        if (badRequestMsg != null) {
            log.info("Request parameters supplied were not valid - {}", badRequestMsg);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, badRequestMsg);
        }

        final List<CourtEvent> listOfCourtEvents;
        if (courtEvents) {
            listOfCourtEvents = movementsRepository.getCourtEvents(agencyIds, fromDateTime, toDateTime);
        } else {
            listOfCourtEvents = List.of();
        }

        final List<ReleaseEvent> listOfReleaseEvents;
        if (releaseEvents) {
            listOfReleaseEvents = movementsRepository.getOffenderReleases(agencyIds, fromDateTime, toDateTime);
        } else {
            listOfReleaseEvents = List.of();
        }

        final List<TransferEvent> listOfTransferEvents;
        if (transferEvents) {
            listOfTransferEvents = movementsRepository.getOffenderTransfers(agencyIds, fromDateTime, toDateTime);
        } else {
            listOfTransferEvents = List.of();
        }

        final List<MovementSummary> listOfMovements;
        if (movements) {
            listOfMovements = movementsRepository.getCompletedMovementsForAgencies(agencyIds, fromDateTime, toDateTime);
        } else {
            listOfMovements = List.of();
        }

        return TransferSummary.builder()
            .courtEvents(listOfCourtEvents)
            .releaseEvents(listOfReleaseEvents)
            .transferEvents(listOfTransferEvents)
            .movements(listOfMovements)
            .build();
    }

    @PreAuthorize("hasRole('SYSTEM_USER')")
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
            ).collect(Collectors.toList());
    }

    private final String checkTransferParameters(final List<String> agencyIds, final LocalDateTime fromDateTime, final LocalDateTime toDateTime,
                                                 final boolean courtEvents, final boolean releaseEvents, final boolean transferEvents,
                                                 final boolean movements) {

        // Needs at least one agency ID specified
        if (CollectionUtils.isEmpty(agencyIds)) {
            return "No agency location identifiers were supplied";
        }

        // The from time must be before the to time
        if (fromDateTime.isAfter(toDateTime)) {
            return "The supplied fromDateTime parameter is after the toDateTime value";
        }

        // The time period requested must be shorter than or equal to 24 hours
        if (toDateTime.isAfter(fromDateTime.plus(24, ChronoUnit.HOURS))) {
            return "The supplied time period is more than 24 hours - limit to 24 hours maximum";
        }

        // One of the event/movement type query parameters must be true
        if (!courtEvents && !releaseEvents && !transferEvents && !movements) {
            return "At least one query parameter must be true [courtEvents|releaseEvents|transferEvents|movements]";
        }

        return null;
    }

    @VerifyBookingAccess
    public Page<OffenderIn> getOffendersIn(final String agencyId, final LocalDateTime fromDate, final LocalDateTime toDate, final Pageable pageable, final boolean allMovements) {
        final var page = allMovements
            ? externalMovementRepository.findAllMovements(agencyId, MovementDirection.IN, fromDate, toDate, pageable)
            : externalMovementRepository.findMovements(agencyId, ActiveFlag.Y, MovementDirection.IN, fromDate, toDate, pageable);
        final var movements = page.getContent().stream().map(this::transform).collect(toList());
        return new PageImpl<OffenderIn>(movements, pageable, page.getTotalElements());
    }

    private OffenderIn transform(ExternalMovement m) {
        final var booking = m.getOffenderBooking();
        final var offender = booking.getOffender();
        final var description = Optional.ofNullable(booking.getAssignedLivingUnit()).map(unit -> firstNonNull(unit.getUserDescription(), unit.getDescription())).orElse(null);
        final var fromAgency = Optional.ofNullable(m.getFromAgency());
        final var toAgency = Optional.ofNullable(m.getToAgency());
        final var fromCityDescription = Optional.ofNullable(m.getFromCity()).map(City::getDescription).orElse(null);
        final var toCityDescription = Optional.ofNullable(m.getToCity()).map(City::getDescription).orElse(null);
        return OffenderIn.builder()
            .offenderNo(offender.getNomsId())
            .bookingId(m.getOffenderBooking().getBookingId())
            .dateOfBirth(offender.getBirthDate())
            .firstName(capitalizeFully(offender.getFirstName()))
            .middleName(capitalizeFully(offender.getMiddleName()))
            .lastName(capitalizeFully(offender.getLastName()))
            .fromAgencyId(fromAgency.map(AgencyLocation::getId).orElse(null))
            .fromAgencyDescription(fromAgency.map(AgencyLocation::getDescription).orElse(null))
            .toAgencyId(toAgency.map(AgencyLocation::getId).orElse(null))
            .toAgencyDescription(toAgency.map(AgencyLocation::getDescription).orElse(null))
            .fromCity(fromCityDescription)
            .toCity(toCityDescription)
            .movementDateTime(m.getMovementTime())
            .movementTime(m.getMovementTime().toLocalTime())
            .location(description)
            .build();
    }
}
