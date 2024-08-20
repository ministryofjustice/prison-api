package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.QuestionnaireRepository
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
class IncidentTypeConfigurationRepositoryTest {

  @Autowired
  private lateinit var repository: QuestionnaireRepository

  @Nested
  inner class RetrieveQuestionnaires {
    @Test
    fun findQuestionnaires() {
      val questionnaires = repository.findAllByCategory("IR_TYPE")

      assertThat(questionnaires).hasSize(24)

      val mapOfTypes = questionnaires.associateBy { it.code }
      val assault = mapOfTypes["ASSAULT"]
      assertThat(assault?.questions).hasSize(28)

      val assaultQuestions = assault?.questions!!.sortedBy { it.listSequence }
      assertThat(assaultQuestions[0].answers).hasSize(2)

      assertThat(assault.offenderRoles).hasSize(2)
    }
  }
}
