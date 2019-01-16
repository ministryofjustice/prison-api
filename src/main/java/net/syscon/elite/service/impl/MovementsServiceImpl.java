package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.MovementsRepository;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.MovementsService;
import net.syscon.elite.service.support.LocationProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MovementsServiceImpl implements MovementsService {

    private final MovementsRepository movementsRepository;


    public MovementsServiceImpl(MovementsRepository movementsRepository) {
        this.movementsRepository = movementsRepository;
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public List<Movement> getRecentMovementsByDate(LocalDateTime fromDateTime, LocalDate movementDate) {
        return movementsRepository.getRecentMovementsByDate(fromDateTime, movementDate);
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'SYSTEM_READ_ONLY', 'GLOBAL_SEARCH')")
    public List<Movement> getRecentMovementsByOffenders(List<String> offenderNumbers, List<String> movementTypes) {
        final var movements = movementsRepository.getRecentMovementsByOffenders(offenderNumbers, movementTypes);

        return movements.stream().map(movement -> movement.toBuilder()
                .fromAgencyDescription(LocationProcessor.formatLocation(movement.getFromAgencyDescription()))
                .toAgencyDescription(LocationProcessor.formatLocation(movement.getToAgencyDescription()))
                .build())
                .collect(Collectors.toList());
    }

    @Override
    @VerifyAgencyAccess
    public List<RollCount> getRollCount(String agencyId, boolean unassigned) {
        return movementsRepository.getRollCount(agencyId, unassigned ? "N" : "Y");
    }

    @Override
    @VerifyAgencyAccess
    public MovementCount getMovementCount(String agencyId, LocalDate date) {
        return movementsRepository.getMovementCount(agencyId, date == null ? LocalDate.now() : date);
    }

    @Override
    @VerifyAgencyAccess
    public List<OffenderOutTodayDto> getOffendersOut(String agencyId, LocalDate movementDate) {

       final var offenders = movementsRepository.getOffendersOut(agencyId, movementDate);

        return offenders
                .stream()
                .map(this::toOffenderOutTodayDto)
                .collect(Collectors.toList());
    }

    private OffenderOutTodayDto toOffenderOutTodayDto(OffenderMovement offenderMovement) {
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

    @Override
    @VerifyAgencyAccess
    public List<OffenderMovement> getEnrouteOffenderMovements(String agencyId, LocalDate date) {

        final var movements = movementsRepository.getEnrouteMovementsOffenderMovementList(agencyId, date);

        return movements.stream().map(movement -> movement.toBuilder()
            .fromAgencyDescription(LocationProcessor.formatLocation(movement.getFromAgencyDescription()))
            .toAgencyDescription(LocationProcessor.formatLocation(movement.getToAgencyDescription()))
            .build())
            .collect(Collectors.toList());

    }

    @Override
    public int getEnrouteOffenderCount(String agencyId, LocalDate date) {
        final LocalDate defaultedDate = date == null ? LocalDate.now() : date;
        return movementsRepository.getEnrouteMovementsOffenderCount(agencyId, defaultedDate);
    }

    @Override
    @VerifyAgencyAccess
    public List<OffenderIn> getOffendersIn(String agencyId, LocalDate date) {
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

    @Override
    @VerifyAgencyAccess
    public List<OffenderInReception> getOffendersInReception(String agencyId) {
        return movementsRepository.getOffendersInReception(agencyId)
                .stream()
                .map(offender -> offender.toBuilder()
                        .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
                        .lastName(WordUtils.capitalizeFully(offender.getLastName()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<OffenderOut> getOffendersCurrentlyOut(long livingUnitId) {
        final var offendersOut = movementsRepository.getOffendersCurrentlyOut(livingUnitId);
        offendersOut.forEach( o -> {
            o.setLastName(WordUtils.capitalizeFully(o.getLastName()));
            o.setFirstName(WordUtils.capitalizeFully(o.getFirstName()));
        });
        return offendersOut;
    }
}
