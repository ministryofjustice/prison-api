package net.syscon.prison.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.api.model.CourtEvent;
import net.syscon.prison.api.model.Movement;
import net.syscon.prison.api.model.MovementCount;
import net.syscon.prison.api.model.MovementSummary;
import net.syscon.prison.api.model.OffenderIn;
import net.syscon.prison.api.model.OffenderInReception;
import net.syscon.prison.api.model.OffenderMovement;
import net.syscon.prison.api.model.OffenderOut;
import net.syscon.prison.api.model.OffenderOutTodayDto;
import net.syscon.prison.api.model.ReleaseEvent;
import net.syscon.prison.api.model.RollCount;
import net.syscon.prison.api.model.TransferEvent;
import net.syscon.prison.api.model.TransferSummary;
import net.syscon.prison.repository.MovementsRepository;
import net.syscon.prison.security.VerifyAgencyAccess;
import net.syscon.prison.security.VerifyBookingAccess;
import net.syscon.prison.service.support.LocationProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
public class MovementsService {

    private final MovementsRepository movementsRepository;
    private final int maxBatchSize;


    public MovementsService(final MovementsRepository movementsRepository, @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.movementsRepository = movementsRepository;
        this.maxBatchSize = maxBatchSize;
    }

    @PreAuthorize("hasAnyRole('SYSTEM_USER','GLOBAL_SEARCH')")
    public List<Movement> getRecentMovementsByDate(final LocalDateTime fromDateTime, final LocalDate movementDate, final List<String> movementTypes) {
        return movementsRepository.getRecentMovementsByDate(fromDateTime, movementDate, movementTypes);
    }

    @VerifyBookingAccess
    public Movement getMovementByBookingIdAndSequence(@NotNull final Long bookingId, @NotNull final Integer sequenceNumber) {
        final var movement = movementsRepository.getMovementByBookingIdAndSequence(bookingId, sequenceNumber);
        return movement.toBuilder()
                .fromAgencyDescription(StringUtils.trimToEmpty(LocationProcessor.formatLocation(movement.getFromAgencyDescription())))
                .toAgencyDescription(StringUtils.trimToEmpty(LocationProcessor.formatLocation(movement.getToAgencyDescription())))
                .toCity(WordUtils.capitalizeFully(StringUtils.trimToEmpty(movement.getToCity())))
                .fromCity(WordUtils.capitalizeFully(StringUtils.trimToEmpty(movement.getFromCity())))
                .build();
    }

    @PreAuthorize("hasAnyRole('SYSTEM_USER','SYSTEM_READ_ONLY','GLOBAL_SEARCH')")
    public List<Movement> getMovementsByOffenders(final List<String> offenderNumbers, final List<String> movementTypes, final boolean latestOnly) {
        final var movements = Lists.partition(offenderNumbers, maxBatchSize)
                .stream()
                .map(offenders -> movementsRepository.getMovementsByOffenders(offenders, movementTypes, latestOnly))
                .flatMap(List::stream);

        return movements.map(movement -> movement.toBuilder()
                .fromAgencyDescription(StringUtils.trimToEmpty(LocationProcessor.formatLocation(movement.getFromAgencyDescription())))
                .toAgencyDescription(StringUtils.trimToEmpty(LocationProcessor.formatLocation(movement.getToAgencyDescription())))
                .toCity(WordUtils.capitalizeFully(StringUtils.trimToEmpty(movement.getToCity())))
                .fromCity(WordUtils.capitalizeFully(StringUtils.trimToEmpty(movement.getFromCity())))
                .build())
                .collect(Collectors.toList());
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
    public List<OffenderOutTodayDto> getOffendersOut(final String agencyId, final LocalDate movementDate) {

        final var offenders = movementsRepository.getOffendersOut(agencyId, movementDate);

        return offenders
                .stream()
                .map(this::toOffenderOutTodayDto)
                .collect(Collectors.toList());
    }

    private OffenderOutTodayDto toOffenderOutTodayDto(final OffenderMovement offenderMovement) {
        return OffenderOutTodayDto
                .builder()
                .dateOfBirth(offenderMovement.getDateOfBirth())
                .firstName(WordUtils.capitalizeFully(offenderMovement.getFirstName()))
                .lastName(WordUtils.capitalizeFully(offenderMovement.getLastName()))
                .reasonDescription(WordUtils.capitalizeFully(offenderMovement.getMovementReasonDescription()))
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
                .collect(Collectors.toList());

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
                        .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
                        .lastName(WordUtils.capitalizeFully(offender.getLastName()))
                        .middleName(WordUtils.capitalizeFully(StringUtils.trimToEmpty(offender.getMiddleName())))
                        .fromAgencyDescription(LocationProcessor.formatLocation(offender.getFromAgencyDescription()))
                        .toAgencyDescription(LocationProcessor.formatLocation(offender.getToAgencyDescription()))
                        .location(StringUtils.trimToEmpty(offender.getLocation()))
                        .build())
                .collect(Collectors.toList());
    }

    @VerifyAgencyAccess
    public List<OffenderInReception> getOffendersInReception(final String agencyId) {
        return movementsRepository.getOffendersInReception(agencyId)
                .stream()
                .map(offender -> offender.toBuilder()
                        .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
                        .lastName(WordUtils.capitalizeFully(offender.getLastName()))
                        .build())
                .collect(Collectors.toList());
    }

    public List<OffenderOut> getOffendersCurrentlyOut(final long livingUnitId) {
        return movementsRepository
                .getOffendersCurrentlyOut(livingUnitId)
                .stream()
                .map(offender -> offender.toBuilder()
                        .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
                        .lastName(WordUtils.capitalizeFully(offender.getLastName()))
                        .build())
                .collect(Collectors.toList());
    }

    public List<OffenderOut> getOffendersCurrentlyOut(final String agencyId) {
        return movementsRepository
                .getOffendersCurrentlyOut(agencyId)
                .stream()
                .map(offender -> offender.toBuilder()
                        .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
                        .lastName(WordUtils.capitalizeFully(offender.getLastName()))
                        .build())
                .collect(Collectors.toList());
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
}
