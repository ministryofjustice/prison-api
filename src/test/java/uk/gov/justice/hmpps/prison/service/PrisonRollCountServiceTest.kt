package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement

class PrisonRollCountServiceTest {

  private val service = PrisonRollCountService(mock(), mock(), mock())

  @Test
  fun `Get duplicate count`() {
    val movements = listOf(
      createMovement("A1001AA", "OUT", "CRT", "1"),
      createMovement("A1001AA", "OUT", "REL", "2"),
      createMovement("A1006AA", "OUT", "CRT", "1"),
      createMovement("A1006AA", "OUT", "REL", "2"),
    )

    val doubleMoveCount = service.getConsecutiveOutMoveCount(movements)
    assertThat(doubleMoveCount).isEqualTo(2)
  }

  @Test
  fun `Get duplicate count when out of order out movement`() {
    val movements = listOf(
      createMovement("A1001AA", "OUT", "CRT", "1"),
      createMovement("A1001AA", "OUT", "REL", "2"),
      createMovement("A1006AA", "OUT", "CRT", "6"),
      createMovement("A1006AA", "OUT", "REL", "8"),
    )

    val doubleMoveCount = service.getConsecutiveOutMoveCount(movements)
    assertThat(doubleMoveCount).isEqualTo(1)
  }

  @Test
  fun `Get duplicate count when out of order out movement and multiple offender sequences overlap`() {
    val movements = listOf(
      createMovement("G8395GQ", "OUT", "TAP", "56"),
      createMovement("G8395GQ", "OUT", "REL", "57"),
      createMovement("G6416UJ", "OUT", "TAP", "4"),
      createMovement("G6416UJ", "OUT", "TAP", "6"),
      createMovement("G3126VH", "OUT", "CRT", "18"),
      createMovement("G3126VH", "OUT", "TRN", "19"),
      createMovement("G1751UN", "OUT", "TAP", "4"),
      createMovement("G1751UN", "OUT", "REL", "5"),
    )

    val doubleMoveCount = service.getConsecutiveOutMoveCount(movements)
    assertThat(doubleMoveCount).isEqualTo(3)
  }

  private fun createMovement(offenderNo: String, directionCode: String, movementType: String, movementSequence: String): OffenderMovement = OffenderMovement.builder()
    .offenderNo(offenderNo)
    .directionCode(directionCode)
    .movementType(movementType)
    .movementSequence(movementSequence)
    .build()
}
