package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import lombok.val;
import net.syscon.elite.api.model.Movement;
import net.syscon.elite.api.model.OffenderInReception;
import net.syscon.elite.api.model.OffenderMovement;
import net.syscon.elite.api.model.OffenderOutTodayDto;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.MovementsRepository;
import net.syscon.elite.service.MovementsService;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Test cases for {@link BookingServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MovementsServiceImplTest {
    private static final String TEST_OFFENDER_NO = "AA1234A";
    @Mock
    private MovementsRepository movementsRepository;

    private MovementsService movementsService;

    @Before
    public void init() {
        movementsService = new MovementsServiceImpl(movementsRepository);
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
        Mockito.when(movementsRepository.getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12), "lastName", Order.DESC)).thenReturn(oms);

        final List<OffenderMovement> enrouteOffenderMovements = movementsService.getEnrouteOffenderMovements("LEI", LocalDate.of(2015, 9, 12), "lastName", Order.DESC);
        Assertions.assertThat(enrouteOffenderMovements).extracting("fromAgencyDescription").contains("Leeds");
        Assertions.assertThat(enrouteOffenderMovements).extracting("toAgencyDescription").contains("Moorlands");
        Assertions.assertThat(enrouteOffenderMovements).extracting("lastName").contains("SMITH");
        Assertions.assertThat(enrouteOffenderMovements).extracting("bookingId").contains(123L);

        Mockito.verify(movementsRepository, Mockito.times(1)).getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12), "lastName", Order.DESC);
    }

    @Test
    public void testGetEnrouteOffenderMovementsDefaultSorting() {

        Mockito.when(movementsRepository.getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12), "lastName,firstName", Order.ASC)).thenReturn(Lists.emptyList());

        /* call service with no specified sorting */
        movementsService.getEnrouteOffenderMovements("LEI", LocalDate.of(2015, 9, 12), null, null);

        Mockito.verify(movementsRepository, Mockito.times(1)).getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12), "lastName,firstName", Order.ASC);
    }

    @Test
    public void testGetEnrouteOffenderNoDateFilter() {

        Mockito.when(movementsRepository.getEnrouteMovementsOffenderMovementList("LEI", null, "lastName,firstName", Order.ASC)).thenReturn(Lists.emptyList());

        /* call service with no specified sorting */
        movementsService.getEnrouteOffenderMovements("LEI", null, null, null);

        Mockito.verify(movementsRepository, Mockito.times(1)).getEnrouteMovementsOffenderMovementList("LEI", null, "lastName,firstName", Order.ASC);
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
        List<OffenderMovement> offenders = ImmutableList.of(
                OffenderMovement.builder()
                        .offenderNo("1234")
                        .directionCode("OUT")
                        .dateOfBirth(LocalDate.now())
                        .movementDate(LocalDate.now())
                        .fromAgency("LEI")
                        .firstName("JOHN")
                        .lastName("DOE")
                        .movementReasonDescription("NORMAL TRANSFER")
                        .movementTime(timeOut)
                        .build());


        Mockito.when(movementsRepository.getOffendersOut("LEI", LocalDate.now())).thenReturn(offenders);

        final List<OffenderOutTodayDto> offendersOutToday = movementsService.getOffendersOut("LEI", LocalDate.now());

        Assertions.assertThat(offendersOutToday).hasSize(1);

        Assertions.assertThat(offendersOutToday).extracting("offenderNo").contains("1234");
        Assertions.assertThat(offendersOutToday).extracting("firstName").contains("John");
        Assertions.assertThat(offendersOutToday).extracting("lastName").contains("Doe");
        Assertions.assertThat(offendersOutToday).extracting("dateOfBirth").contains(LocalDate.now());
        Assertions.assertThat(offendersOutToday).extracting("timeOut").contains(timeOut);
        Assertions.assertThat(offendersOutToday).extracting("reasonDescription").contains("Normal transfer");
    }

    @Test
    public void testMappingToProperCase() {
      String agency = "LEI";

      Mockito.when(movementsRepository.getOffendersInReception(agency))
              .thenReturn(
                      Arrays.asList(OffenderInReception.builder()
                              .firstName("FIRST")
                              .lastName("LASTNAME")
                              .dateOfBirth(LocalDate.of(1950,10,10))
                              .offenderNo("1234A")
                              .build()
                      )
              );

        final val offenders = movementsService.getOffendersInReception(agency);

        Assertions.assertThat(offenders)
                .containsExactly(OffenderInReception.builder()
                .firstName("First")
                .lastName("Lastname")
                .dateOfBirth(LocalDate.of(1950,10,10))
                .offenderNo("1234A")
                .build());
    }


}
