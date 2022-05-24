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
import uk.gov.justice.hmpps.prison.api.support.TimeSlot
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
      val counts = repository.getCountActivities(
        "LEI",
        LocalDate.parse("2017-09-11"),
        LocalDate.parse("2017-09-28"),
        listOf(TimeSlot.AM.name, TimeSlot.PM.name)
      )
      assertThat(counts).isEqualTo(PrisonerActivitiesCount(76, 8))
    }

    @Test
    fun `get count activities pm only`() {
      val counts = repository.getCountActivities(
        "LEI",
        LocalDate.parse("2017-09-11"),
        LocalDate.parse("2017-09-28"),
        listOf(TimeSlot.PM.name)
      )
      assertThat(counts).isEqualTo(PrisonerActivitiesCount(32, 0))
    }
  }
}
