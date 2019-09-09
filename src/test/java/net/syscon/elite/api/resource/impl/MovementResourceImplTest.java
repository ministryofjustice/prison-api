package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.MovementResource;
import net.syscon.elite.service.MovementsService;
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

        verify(movementsService).getMovementsByOffenders(List.of(), List.of(), false);
    }

    @Test
    public void getMovementsByOffenders() {
        movementResource.getMovementsByOffenders(List.of(), List.of(), true);

        verify(movementsService).getMovementsByOffenders(List.of(), List.of(), true);
    }
}
