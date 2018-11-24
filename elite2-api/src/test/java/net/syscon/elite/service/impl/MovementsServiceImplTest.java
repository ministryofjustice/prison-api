package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.Movement;
import net.syscon.elite.repository.MovementsRepository;
import net.syscon.elite.service.MovementsService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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
}
