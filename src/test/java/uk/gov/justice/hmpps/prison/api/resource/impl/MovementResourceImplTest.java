package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.CreateExternalMovement;
import uk.gov.justice.hmpps.prison.api.resource.MovementResource;
import uk.gov.justice.hmpps.prison.service.MovementsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MovementResourceImplTest {
    @Mock
    private MovementsService movementsService;

    private MovementResource movementResource;

    @BeforeEach
    public void setUp() {
        movementResource = new MovementResource(movementsService);
    }

    @Test
    public void getMovementsByOffenders_defaultLatestOnly() {
        movementResource.getMovementsByOffenders(List.of(), List.of(), null, true);

        verify(movementsService).getMovementsByOffenders(List.of(), List.of(), true, true);
    }

    @Test
    public void getMovementsByOffenders() {
        movementResource.getMovementsByOffenders(List.of(), List.of(), true, false);

        verify(movementsService).getMovementsByOffenders(List.of(), List.of(), true, false);
    }

    @Test
    public void getMovementsByOffenders_falseLatestOnly() {
        movementResource.getMovementsByOffenders(List.of(), List.of(), false, false);

        verify(movementsService).getMovementsByOffenders(List.of(), List.of(), false, false);
    }

    @Test
    public void createExternalMovement() {
        final var externalMovement = CreateExternalMovement.builder()
            .bookingId(1L)
            .movementType("REL")
            .movementReason("CR")
            .directionCode("OUT")
            .fromAgencyId("HAZLWD")
            .toAgencyId("OUT")
            .movementTime(LocalDateTime.now())
            .build();

        movementResource.createExternalMovement(externalMovement);

        verify(movementsService).createExternalMovement(1L, externalMovement);
    }
}
