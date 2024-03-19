package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
@WithMockUser
class AssessmentRepositoryTest {
  @Autowired
  private lateinit var repository: AssessmentRepository

  @Test
  fun csraAssessmentQuestions() {
    val assessmentQuestions = repository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-4L)
    assertThat(assessmentQuestions).usingRecursiveComparison()
      .ignoringFields("parentAssessment", "createDatetime", "createUserId").isEqualTo(
        listOf(
          AssessmentEntry.builder()
            .assessmentId(-21L)
            .description("Reason for review")
            .listSeq(1L)
            .cellSharingAlertFlag("N")
            .assessmentCode("1")
            .build(),
          AssessmentEntry.builder()
            .assessmentId(-24L)
            .description("Risk of harming a cell mate:")
            .listSeq(2L)
            .cellSharingAlertFlag("N")
            .assessmentCode("2")
            .build(),
          AssessmentEntry.builder()
            .assessmentId(-29L)
            .description("Outcome of review:")
            .listSeq(3L)
            .cellSharingAlertFlag("N")
            .assessmentCode("3")
            .build(),
        ),
      )
    assertThat(assessmentQuestions[0].parentAssessment.assessmentId).isEqualTo(-11L)
  }

  @Test
  fun csraAssessmentQuestions_ReturnsNothing_WhenNotCsraAssessment() {
    val assessmentQuestions = repository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-2L)
    assertThat(assessmentQuestions).isEmpty()
  }
}
