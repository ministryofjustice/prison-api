package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.MovementsRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.MovementsService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Test cases for {@link BookingServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MovementsServiceImplTest {
    private static final String TEST_OFFENDER_NO = "AA1234A";
    @Mock
    private MovementsRepository movementsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    private MovementsService movementsService;

    @Before
    public void init() {
        movementsService = new MovementsServiceImpl(movementsRepository, authenticationFacade, userRepository);
    }

    @Test
    public void testGetRecentMovementsByOffenders() {
        List<Movement> movements = ImmutableList.of(Movement.builder().offenderNo(TEST_OFFENDER_NO).fromAgencyDescription("LEEDS").toAgencyDescription("BLACKBURN").build());
        final ImmutableList<String> offenderNoList = ImmutableList.of(TEST_OFFENDER_NO);
        Mockito.when(movementsRepository.getRecentMovementsByOffenders(offenderNoList,null)).thenReturn(movements);

        final List<Movement> processedMovements = movementsService.getRecentMovementsByOffenders(offenderNoList, null);
        Assertions.assertThat(processedMovements).extracting("toAgencyDescription").containsExactly("Blackburn");
        Assertions.assertThat(processedMovements).extracting("fromAgencyDescription").containsExactly("Leeds");

        Mockito.verify(movementsRepository, Mockito.times(1)).getRecentMovementsByOffenders(offenderNoList,null);
    }

    @Test
    public void testGetRecentMovementsByOffendersNullDescriptions() {
        List<Movement> movements = ImmutableList.of(Movement.builder().offenderNo(TEST_OFFENDER_NO).build());
        final ImmutableList<String> offenderNoList = ImmutableList.of(TEST_OFFENDER_NO);
        Mockito.when(movementsRepository.getRecentMovementsByOffenders(offenderNoList,null)).thenReturn(movements);

        final List<Movement> processedMovements = movementsService.getRecentMovementsByOffenders(offenderNoList, null);
        Assertions.assertThat(processedMovements).extracting("fromAgencyDescription").containsNull();

        Mockito.verify(movementsRepository, Mockito.times(1)).getRecentMovementsByOffenders(offenderNoList,null);
    }

    @Test
    public void testGetEnrouteOffenderMovements() {
        List<OffenderMovement> oms = ImmutableList.of(OffenderMovement.builder()
                .offenderNo(TEST_OFFENDER_NO)
                .bookingId(123L).firstName("JAMES")
                .lastName("SMITH")
                .fromAgencyDescription("LEEDS")
                .toAgencyDescription("MOORLANDS")
                .build());
        Mockito.when(movementsRepository.getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12))).thenReturn(oms);

        final List<OffenderMovement> enrouteOffenderMovements = movementsService.getEnrouteOffenderMovements("LEI", LocalDate.of(2015, 9, 12));
        Assertions.assertThat(enrouteOffenderMovements).extracting("fromAgencyDescription").contains("Leeds");
        Assertions.assertThat(enrouteOffenderMovements).extracting("toAgencyDescription").contains("Moorlands");
        Assertions.assertThat(enrouteOffenderMovements).extracting("lastName").contains("SMITH");
        Assertions.assertThat(enrouteOffenderMovements).extracting("bookingId").contains(123L);

        Mockito.verify(movementsRepository, Mockito.times(1)).getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12));
    }
    @Test
    public void testGetEnrouteOffenderMovementsCount() {
        Mockito.when(movementsRepository.getEnrouteMovementsOffenderCount("LEI", LocalDate.of(2015, 9, 12))).thenReturn(5);

        final int count = movementsService.getEnrouteOffenderCount("LEI", LocalDate.of(2015, 9, 12));
        Assertions.assertThat(count).isEqualTo(5);

        Mockito.verify(movementsRepository, Mockito.times(1)).getEnrouteMovementsOffenderCount("LEI", LocalDate.of(2015, 9, 12));
    }

    @Test
    public void testGetOffendersOutToday() {
        final LocalTime timeOut = LocalTime.now();
        List<OffenderOutToday> offenders = ImmutableList.of(
                OffenderOutToday.builder()
                        .offenderNo("1234")
                        .directionCode("OUT")
                        .birthDate(LocalDate.now())
                        .movementDate(LocalDate.now())
                        .fromAgency("LEI")
                        .firstName("JOHN")
                        .lastName("DOE")
                        .reasonDescription("NORMAL TRANSFER")
                        .timeOut(timeOut)
                        .build(),
                OffenderOutToday.builder()
                        .offenderNo("89876")
                        .directionCode("IN")
                        .birthDate(LocalDate.now())
                        .movementDate(LocalDate.now())
                        .fromAgency("LEI")
                        .firstName("JOHN1")
                        .lastName("DOE1")
                        .reasonDescription("NORMAL TRANSFER")
                        .timeOut(timeOut)
                        .build(),
                OffenderOutToday.builder()
                        .offenderNo("4567")
                        .directionCode("OUT")
                        .birthDate(LocalDate.now())
                        .movementDate(LocalDate.now())
                        .fromAgency("BMI")
                        .firstName("JOHN2")
                        .lastName("DOE2")
                        .reasonDescription("NORMAL TRANSFER")
                        .timeOut(timeOut)
                        .build());


        Mockito.when(userRepository.findByUsername("ITAG_USER")).thenReturn(Optional.of(UserDetail.builder().activeCaseLoadId("LEI").build()));
        Mockito.when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        Mockito.when(movementsRepository.getOffendersOutOnDate(LocalDate.now())).thenReturn(offenders);

        final List<OffenderOutTodayDto> offendersOutToday = movementsService.getOffendersOutToday();

        Assertions.assertThat(offendersOutToday).hasSize(1);

        Assertions.assertThat(offendersOutToday).extracting("offenderNo").contains("1234");
        Assertions.assertThat(offendersOutToday).extracting("firstName").contains("John");
        Assertions.assertThat(offendersOutToday).extracting("lastName").contains("Doe");
        Assertions.assertThat(offendersOutToday).extracting("birthDate").contains(LocalDate.now());
        Assertions.assertThat(offendersOutToday).extracting("timeOut").contains(timeOut);
        Assertions.assertThat(offendersOutToday).extracting("reasonDescription").contains("Normal transfer");
    }


}
