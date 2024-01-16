package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessmentItem
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
@WithMockUser
class OffenderAssessmentRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderAssessmentRepository

  @Test
  fun getAssessmentByBookingIdAndAssessmentSeq() {
    val assessment = repository.findByBookingIdAndAssessmentSeq(-43L, 2).orElseThrow()
    assertCsraAssessment_Booking43_AssessmentSeq2(assessment)
    val expectedQuestion1 = "Reason for review"
    val expectedAnswer1 = "Scheduled"
    val expectedQuestion2 = "Risk of harming a cell mate:"
    val expectedAnswer2 = "Standard"
    val expectedQuestion3 = "Outcome of review:"
    val expectedAnswer3 = "A new plan must be agreed"
    assertThat(assessment.assessmentItems).usingRecursiveComparison()
      .ignoringFields(
        "createDatetime",
        "createUserId",
        "assessmentAnswer.assessmentCode",
        "assessmentAnswer.cellSharingAlertFlag",
        "assessmentAnswer.createDatetime",
        "assessmentAnswer.createUserId",
        // AssertJ cannot handle recursive properties - we will check the assessmentAnswer.parentAssessment separately
        "assessmentAnswer.listSeq",
        "assessmentAnswer.parentAssessment",
      )
      .isEqualTo(
        listOf(
          OffenderAssessmentItem.builder()
            .bookingId(-43L)
            .assessmentSeq(2)
            .itemSeq(1)
            .assessmentAnswer(
              AssessmentEntry.builder()
                .assessmentId(-22L)
                .description(expectedAnswer1)
                .build(),
            )
            .build(),
          OffenderAssessmentItem.builder()
            .bookingId(-43L)
            .assessmentSeq(2)
            .itemSeq(2)
            .assessmentAnswer(
              AssessmentEntry.builder()
                .assessmentId(-28L)
                .description(expectedAnswer2)
                .build(),
            )
            .build(),
          OffenderAssessmentItem.builder()
            .bookingId(-43L)
            .assessmentSeq(2)
            .itemSeq(3)
            .assessmentAnswer(
              AssessmentEntry.builder()
                .assessmentId(-32L)
                .description(expectedAnswer3)
                .build(),
            )
            .build(),
        ),
      )

    // Check each assessmentAnswer's parentAssessments contains the question
    val parentAssessmentByAssessmentAnswer = HashMap<String, AssessmentEntry>()
    assessment.assessmentItems.forEach { a: OffenderAssessmentItem -> parentAssessmentByAssessmentAnswer[a.assessmentAnswer.description] = a.assessmentAnswer.parentAssessment }
    assertThat(parentAssessmentByAssessmentAnswer[expectedAnswer1]!!.description).isEqualTo(expectedQuestion1)
    assertThat(parentAssessmentByAssessmentAnswer[expectedAnswer2]!!.description).isEqualTo(expectedQuestion2)
    assertThat(parentAssessmentByAssessmentAnswer[expectedAnswer3]!!.description).isEqualTo(expectedQuestion3)
  }

  @Test
  fun assessmentByBookingIdAndAssessmentSeq_ReturnsNothing() {
    val assessment = repository.findByBookingIdAndAssessmentSeq(-43L, 11)
    assertThat(assessment).isEmpty()
  }

  @Test
  fun assessmentsByCsraAssessmentAndOffenderNos() {
    val assessments = repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("A1183JE", "A1184MA", "A1184JR"))
    assertThat(assessments).extracting("bookingId", "assessmentSeq")
      .containsExactly(
        tuple(-41L, 2),
        tuple(-57L, 1),
        tuple(-43L, 2),
        tuple(-43L, 1),
      )
    assertCsraAssessment_Booking43_AssessmentSeq2(assessments[2])
  }

  @Test
  fun assessmentsByCsraAssessmentAndOffenderNos_ReturnsNothing() {
    val assessments = repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("A1183JC"))
    assertThat(assessments).isEmpty()
  }

  @Test
  fun assessmentDetailsByOffenderNos() {
    val assessments = repository.findWithDetailsByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("A1183JE", "A1184MA", "A1184JR"))
    assertThat(assessments).extracting("bookingId", "assessmentSeq")
      .containsExactly(
        tuple(-41L, 2),
        tuple(-57L, 1),
        tuple(-43L, 2),
        tuple(-43L, 1),
      )
    assertCsraAssessment_Booking43_AssessmentSeq2(assessments[2])
  }

  @Test
  fun assessmentDetailsByOffenderNos_ReturnsNothing() {
    val assessments = repository.findWithDetailsByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("A1183JC"))
    assertThat(assessments).isEmpty()
  }

  private fun assertCsraAssessment_Booking43_AssessmentSeq2(assessment: OffenderAssessment) {
    assertThat(assessment.bookingId).isEqualTo(-43L)
    assertThat(assessment.assessmentSeq).isEqualTo(2L)
    assertThat(assessment.offenderBooking.bookingId).isEqualTo(-43L)
    assertThat(assessment.calculatedClassification.code).isEqualTo("STANDARD")
    assertThat(assessment.overridingClassification.code).isEqualTo("HI")
    assertThat(assessment.reviewedClassification.code).isEqualTo("HI")
    assertThat(assessment.assessmentDate).isEqualTo(LocalDate.parse("2019-01-04"))
    assertThat(assessment.assessmentCreateLocation.id).isEqualTo("LEI")
    assertThat(assessment.assessmentComment).isEqualTo("A Comment")
    assertThat(assessment.assessCommittee.code).isEqualTo("RECP")
    assertThat(assessment.assessCommittee.description).isEqualTo("Reception")
    assertThat(assessment.assessStatus).isEqualTo("A")
    assertThat(assessment.overrideReason.code).isEqualTo("PREVIOUS")
    assertThat(assessment.overrideReason.description).isEqualTo("Previous History")
    assertThat(assessment.reviewCommittee.code).isEqualTo("GOV")
    assertThat(assessment.reviewCommittee.description).isEqualTo("Governor")
    assertThat(assessment.reviewCommitteeComment).isEqualTo("Review soon")
    assertThat(assessment.nextReviewDate).isEqualTo(LocalDate.parse("2019-11-22"))
    assertThat(assessment.evaluationDate).isEqualTo(LocalDate.parse("2016-07-07"))
    assertThat(assessment.creationUser.username).isEqualTo("JBRIEN")
    assertThat(assessment.assessmentType.assessmentId).isEqualTo(-4L)
    val classificationSummary = assessment.classificationSummary
    assertThat(classificationSummary.finalClassification.code).isEqualTo("HI")
    assertThat(classificationSummary.originalClassification.code).isEqualTo("STANDARD")
    assertThat(classificationSummary.classificationApprovalReason).isEqualTo("Previous History")
  }
}
