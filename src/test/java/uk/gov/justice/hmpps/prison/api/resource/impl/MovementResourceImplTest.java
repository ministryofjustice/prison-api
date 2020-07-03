package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.hmpps.prison.api.resource.MovementResource;
import uk.gov.justice.hmpps.prison.service.MovementsService;

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
