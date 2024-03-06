package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExternalMovementRepositoryTest {
  @Autowired
  private lateinit var externalMovementRepository: ExternalMovementRepository

  @Autowired
  private lateinit var movementTypeRepository: ReferenceCodeRepository<MovementType>

  @Test
  fun queryShouldReturnExpectedResult() {
    val temporaryAbsenceMovementType = movementTypeRepository.findById(MovementType.TAP).orElseThrow()
    val absences =
      externalMovementRepository.findCurrentTemporaryAbsencesForPrison("LEI", temporaryAbsenceMovementType)
    assertThat(absences).hasSize(1)
    assertThat(absences).extracting(
      "offenderBooking.bookingId",
      "movementDirection",
      "movementType.code",
    ).containsExactly(tuple(-25L, MovementDirection.OUT, "TAP"))
  }
}
