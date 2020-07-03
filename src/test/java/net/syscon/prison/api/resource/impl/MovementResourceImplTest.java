package net.syscon.prison.api.resource.impl;

import net.syscon.prison.api.resource.MovementResource;
import net.syscon.prison.service.MovementsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MovementResourceImplTest {
    @Mock
    private MovementsService movementsService;

    private MovementResource movementResource;

    @Before
    public void setUp() {
        movementResource = new MovementResourceImpl(movementsService);
    }

    @Test
    public void getMovementsByOffenders_defaultLatestOnly() {
        movementResource.getMovementsByOffenders(List.of(), List.of(), null);

        verify(movementsService).getMovementsByOffenders(List.of(), List.of(), true);
    }

    @Test
    public void getMovementsByOffenders() {
        movementResource.getMovementsByOffenders(List.of(), List.of(), true);

        verify(movementsService).getMovementsByOffenders(List.of(), List.of(), true);
    }

    @Test
    public void getMovementsByOffenders_falseLatestOnly() {
        movementResource.getMovementsByOffenders(List.of(), List.of(), false);

        verify(movementsService).getMovementsByOffenders(List.of(), List.of(), false);
    }
}
