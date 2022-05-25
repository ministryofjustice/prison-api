@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
class PrisonerActivitiesCountRepositoryTest {
  @Autowired
  private lateinit var repository: PrisonerActivitiesCountRepository

  @Nested
  inner class getCountActivities {
    @Test
    fun `get count activities am and pm`() {
      val activities = repository.getActivities(
        "LEI",
        LocalDate.parse("2017-09-11"),
        LocalDate.parse("2017-09-28"),
      )
      assertThat(activities.map { it.bookingId }).hasSameElementsAs(listOf(-1L, -2L, -3L, -4L, -5L, -6L, -35L, -40L))
    }
  }
}
