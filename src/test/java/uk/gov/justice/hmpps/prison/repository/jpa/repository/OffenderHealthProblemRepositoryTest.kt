package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffenderHealthProblemRepositoryTest(
  @Autowired private val repository: OffenderHealthProblemRepository,
) {
  @Test
  fun findAllByOffenderBookingOffenderNomsIdInAndOffenderBookingBookingSequenceAndProblemTypeCodeAndStartDateBetween() {
    val list = repository.findAllByOffenderBookingOffenderNomsIdInAndOffenderBookingBookingSequenceAndProblemTypeCodeAndStartDateBetween(
      listOf("A1234AD", "A1234AC"),
      1,
      "DISAB",
      LocalDate.of(2010, 6, 22),
      LocalDate.of(2010, 6, 26),
    )
    assertThat(list).hasSize(2)
    assertThat(list).extracting<String, RuntimeException> { it.problemType.code }.containsOnly("DISAB")
  }
}
