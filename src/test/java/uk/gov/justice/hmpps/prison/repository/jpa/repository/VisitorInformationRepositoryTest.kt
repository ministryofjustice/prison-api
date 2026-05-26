package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
class VisitorInformationRepositoryTest(
  @Autowired private val repository: VisitorRepository,
) {
  @Test
  fun findAllByVisitId() {
    val visits = repository.findAllByVisitId(-15L)

    assertThat(visits)
      .extracting<Long, RuntimeException> { it.personId }
      .containsOnly(-1L, -2L)
    assertThat(visits)
      .extracting<String, RuntimeException> { it.firstName }
      .containsOnly("JESSY", "John")
    assertThat(visits)
      .extracting<String, RuntimeException> { it.lastName }
      .containsOnly("SMITH1", "Smith")
    assertThat(visits)
      .extracting<String, RuntimeException> { it.eventOutcome }
      .containsOnly("ABS", "ATT")
  }
}
