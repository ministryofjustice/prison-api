package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffenderImageRepositoryTest(
  @Autowired private val repository: OffenderImageRepository,
) {
  @Test
  fun getImagesByOffenderNumber() {
    val images = repository.getImagesByOffenderNumber("A1069AA")

    assertThat(images).hasSize(4)
    assertThat(images)
      .extracting<Long, RuntimeException> { it.getId() }
      .containsOnly(-100L, -101L, -102L, -103L)
  }

  @Test
  fun getImagesByOffenderNumberReturnsEmptyForOffenderNumberNotFound() {
    assertThat(repository.getImagesByOffenderNumber("unknown")).isEmpty()
  }
}
