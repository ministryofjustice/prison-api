package net.syscon.elite.service.impl;

import lombok.val;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.MovementsRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.MovementsService;
import net.syscon.elite.service.support.LocationProcessor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringUtils;
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
    private final AuthenticationFacade authenticationFacade;
    private final UserRepository userRepository;


    public MovementsServiceImpl(MovementsRepository movementsRepository, AuthenticationFacade authenticationFacade, UserRepository userRepository) {
        this.movementsRepository = movementsRepository;
        this.authenticationFacade = authenticationFacade;
        this.userRepository = userRepository;
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public List<Movement> getRecentMovementsByDate(LocalDateTime fromDateTime, LocalDate movementDate) {
        return movementsRepository.getRecentMovementsByDate(fromDateTime, movementDate);
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public List<Movement> getRecentMovementsByOffenders(List<String> offenderNumbers, List<String> movementTypes) {
        final List<Movement> movements = movementsRepository.getRecentMovementsByOffenders(offenderNumbers, movementTypes);
        movements.forEach(m -> {
            m.setFromAgencyDescription(LocationProcessor.formatLocation(m.getFromAgencyDescription()));
            m.setToAgencyDescription(LocationProcessor.formatLocation(m.getToAgencyDescription()));
        });
        return movements;
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
    public List<OffenderOutTodayDto> getOffendersOutToday() {
        String username = authenticationFacade.getCurrentUsername();
        UserDetail currentUser = userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));

        List<OffenderOutToday> offenders = movementsRepository.getOffendersOutOnDate(LocalDate.now());

        return offenders
                .stream()
                .filter(offender -> offender.getDirectionCode().equals("OUT") &&
                        offender.getFromAgency().equals(currentUser.getActiveCaseLoadId()))
                .map(this::toOffenderOutTodayDto)
                .collect(Collectors.toList());
    }

    private OffenderOutTodayDto toOffenderOutTodayDto(OffenderOutToday offenderOutToday) {
        return OffenderOutTodayDto
                .builder()
                .birthDate(offenderOutToday.getBirthDate())
                .firstName(StringUtils.capitalize(offenderOutToday.getFirstName().toLowerCase()))
                .lastName(StringUtils.capitalize(offenderOutToday.getLastName().toLowerCase()))
                .reasonDescription(StringUtils.capitalize(offenderOutToday.getReasonDescription().toLowerCase()))
                .offenderNo(offenderOutToday.getOffenderNo())
                .timeOut(offenderOutToday.getTimeOut())
                .build();
    }

    @Override
    @VerifyAgencyAccess
    public List<OffenderMovement> getEnrouteOffenderMovements(String agencyId, LocalDate date, String orderByFields, Order order) {
        String sortFields = StringUtils.defaultString(orderByFields, "lastName,firstName");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);
        final LocalDate defaultedDate = date == null ? LocalDate.now() : date;
        final List<OffenderMovement> movements = movementsRepository.getEnrouteMovementsOffenderMovementList(agencyId, defaultedDate, sortFields, sortOrder);
        movements.forEach(m -> {
            m.setFromAgencyDescription(LocationProcessor.formatLocation(m.getFromAgencyDescription()));
            m.setToAgencyDescription(LocationProcessor.formatLocation(m.getToAgencyDescription()));
        });
        return movements;
    }

    @Override
    public int getEnrouteOffenderCount(String agencyId, LocalDate date) {
        final LocalDate defaultedDate = date == null ? LocalDate.now() : date;
        return movementsRepository.getEnrouteMovementsOffenderCount(agencyId, defaultedDate);
    }

    @Override
    @VerifyAgencyAccess
    public List<OffenderIn> getOffendersIn(String agencyId, LocalDate date) {
        val offendersIn = movementsRepository.getOffendersIn(agencyId, date);
        offendersIn.forEach(oi -> oi.setFromAgencyDescription(LocationProcessor.formatLocation(oi.getFromAgencyDescription())));  // meh
        return offendersIn;
    }
}
