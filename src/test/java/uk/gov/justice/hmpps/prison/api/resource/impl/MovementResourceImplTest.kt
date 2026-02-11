package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.hmpps.prison.api.resource.MovementResource
import uk.gov.justice.hmpps.prison.service.MovementsService

@ExtendWith(MockitoExtension::class)
class MovementResourceImplTest {
  private val movementsService: MovementsService = mock()
  private var movementResource: MovementResource = MovementResource(movementsService)

  @Test
  fun getMovementsByOffenders_defaultLatestOnly() {
    movementResource.getMovementsByOffenders(mutableListOf(), mutableListOf(), null, true)

    verify(movementsService)
      .getMovementsByOffenders(mutableListOf(), mutableListOf(), true, true)
  }

  @Test
  fun getMovementsByOffenders() {
    movementResource.getMovementsByOffenders(mutableListOf(), mutableListOf(), true, false)

    verify(movementsService)
      .getMovementsByOffenders(mutableListOf(), mutableListOf(), true, false)
  }

  @Test
  fun getMovementsByOffenders_falseLatestOnly() {
    movementResource.getMovementsByOffenders(mutableListOf(), mutableListOf(), false, false)

    verify(movementsService)
      .getMovementsByOffenders(mutableListOf(), mutableListOf(), false, false)
  }
}
