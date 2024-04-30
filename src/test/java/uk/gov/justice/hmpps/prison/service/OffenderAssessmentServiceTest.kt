package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.AssessmentDetail
import uk.gov.justice.hmpps.prison.api.model.AssessmentQuestion
import uk.gov.justice.hmpps.prison.api.model.AssessmentSummary
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentClassification
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentCommittee
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentOverrideReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessmentItem
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AssessmentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAssessmentRepository
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class OffenderAssessmentServiceTest {

  val repository: OffenderAssessmentRepository = mock()
  private val assessmentRepository: AssessmentRepository = mock()
  var service = OffenderAssessmentService(repository, assessmentRepository, 1000)

  @BeforeEach
  fun beforeEach() {
    service = OffenderAssessmentService(repository, assessmentRepository, 1000)
  }

  @Test
  fun offenderAssessment_returnsCorrectApiObject() {
    whenever(repository.findByBookingIdAndAssessmentSeq(-1L, 2)).thenReturn(
      Optional.of(
        OffenderAssessment.builder()
          .bookingId(-1L)
          .assessmentSeq(2)
          .offenderBooking(
            OffenderBooking.builder()
              .offender(
                Offender.builder()
                  .nomsId("NN123N")
                  .build(),
              )
              .build(),
          )
          .assessmentType(
            AssessmentEntry.builder()
              .assessmentId(-11L)
              .assessmentCode("CSRREV1")
              .build(),
          )
          .assessmentItems(
            listOf(
              OffenderAssessmentItem.builder()
                .assessmentAnswer(
                  AssessmentEntry.builder()
                    .description("Answer 1")
                    .parentAssessment(
                      AssessmentEntry.builder()
                        .assessmentId(-10L)
                        .build(),
                    )
                    .build(),
                )
                .build(),
            ),
          )
          .calculatedClassification(AssessmentClassification("STANDARD", "Standard"))
          .overridingClassification(AssessmentClassification("HI", "High"))
          .reviewedClassification(AssessmentClassification("HI", "High"))
          .assessmentDate(LocalDate.parse("2019-01-02"))
          .assessmentCreateLocation(
            AgencyLocation.builder()
              .id("LEI")
              .build(),
          )
          .assessmentComment("Assessment Comment 1")
          .assessCommittee(AssessmentCommittee("RECP", "Reception"))
          .creationUser(
            StaffUserAccount.builder()
              .username("JBRIEN")
              .build(),
          )
          .evaluationDate(LocalDate.parse("2019-01-03"))
          .overrideReason(AssessmentOverrideReason("OVERRIDE_DUMMY_VALUE", "Review reason"))
          .reviewCommittee(AssessmentCommittee("REVW", "Review board"))
          .reviewCommitteeComment("Review comment")
          .nextReviewDate(LocalDate.parse("2020-01-02"))
          .build(),
      ),
    )
    whenever(assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-11L)).thenReturn(
      listOf(
        AssessmentEntry.builder()
          .description("Question 1")
          .assessmentId(-10L)
          .build(),
        AssessmentEntry.builder()
          .description("Question 2")
          .assessmentId(-9L)
          .build(),
      ),
    )
    val assessmentApiObject = service.getOffenderAssessment(-1L, 2)
    assertThat(assessmentApiObject).isEqualTo(
      AssessmentDetail.detailBuilder()
        .summary(
          AssessmentSummary.builder()
            .bookingId(-1L)
            .assessmentSeq(2)
            .offenderNo("NN123N")
            .classificationCode("HI")
            .assessmentCode("CSRREV1")
            .cellSharingAlertFlag(true)
            .assessmentDate(LocalDate.parse("2019-01-02"))
            .assessmentAgencyId("LEI")
            .assessmentComment("Assessment Comment 1")
            .assessorUser("JBRIEN")
            .nextReviewDate(LocalDate.parse("2020-01-02"))
            .build(),
        )
        .assessmentCommitteeCode("RECP")
        .assessmentCommitteeName("Reception")
        .approvalDate(LocalDate.parse("2019-01-03"))
        .approvalCommitteeCode("REVW")
        .approvalCommitteeName("Review board")
        .originalClassificationCode("STANDARD")
        .classificationReviewReason("Review reason")
        .calculatedClassificationCode("STANDARD")
        .overridingClassificationCode("HI")
        .approvedClassificationCode("HI")
        .overrideReason("Review reason")
        .approvalComment("Review comment")
        .questions(
          listOf(
            AssessmentQuestion.builder()
              .question("Question 1")
              .answer("Answer 1")
              .additionalAnswers(listOf())
              .build(),
            AssessmentQuestion.builder()
              .question("Question 2")
              .build(),
          ),
        )
        .build(),
    )
  }

  @Test
  fun offenderAssessment_throwsEntityNotFoundIfNoMatch() {
    whenever(repository.findByBookingIdAndAssessmentSeq(anyLong(), anyInt())).thenReturn(Optional.empty())
    assertThatThrownBy { service.getOffenderAssessment(-1L, 2) }.isInstanceOf(EntityNotFoundException::class.java)
  }

  @Test
  fun offenderAssessment_throwsEntityNotFoundIfNoCsraQuestions() {
    whenever(repository.findByBookingIdAndAssessmentSeq(anyLong(), anyInt())).thenReturn(
      Optional.of(
        getOffenderAssessment_MinimalBuilder(-1L, 2, "NN123N", -11L)
          .build(),
      ),
    )
    whenever(assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-11L)).thenReturn(listOf())
    assertThatThrownBy { service.getOffenderAssessment(-1L, 2) }.isInstanceOf(EntityNotFoundException::class.java)
  }

  @Test
  fun offenderAssessment_handlesMultipleAnswersToCsraQuestion() {
    whenever(repository.findByBookingIdAndAssessmentSeq(anyLong(), anyInt())).thenReturn(
      Optional.of(
        getOffenderAssessment_MinimalBuilder(-1L, 2, "NN123N", -11L)
          .assessmentItems(
            listOf(
              OffenderAssessmentItem.builder()
                .assessmentAnswer(
                  AssessmentEntry.builder()
                    .description("First answer")
                    .parentAssessment(
                      AssessmentEntry.builder()
                        .assessmentId(-10L)
                        .build(),
                    )
                    .build(),
                )
                .build(),
              OffenderAssessmentItem.builder()
                .assessmentAnswer(
                  AssessmentEntry.builder()
                    .description("Second answer")
                    .parentAssessment(
                      AssessmentEntry.builder()
                        .assessmentId(-10L)
                        .build(),
                    )
                    .build(),
                )
                .build(),
              OffenderAssessmentItem.builder()
                .assessmentAnswer(
                  AssessmentEntry.builder()
                    .description("Third answer")
                    .parentAssessment(
                      AssessmentEntry.builder()
                        .assessmentId(-10L)
                        .build(),
                    )
                    .build(),
                )
                .build(),
            ),
          )
          .build(),
      ),
    )
    whenever(assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-11L)).thenReturn(
      listOf(
        AssessmentEntry.builder()
          .description("Multiple answer question")
          .assessmentId(-10L)
          .build(),
      ),
    )
    val assessment = service.getOffenderAssessment(-1L, 2)
    assertThat(assessment.questions).isEqualTo(
      listOf(
        AssessmentQuestion.builder()
          .question("Multiple answer question")
          .answer("First answer")
          .additionalAnswers(
            listOf(
              "Second answer",
              "Third answer",
            ),
          )
          .build(),
      ),
    )
  }

  @Test
  fun offenderAssessment_ignoresAnswersThatAreNotInCsraQuestions() {
    whenever(repository.findByBookingIdAndAssessmentSeq(anyLong(), anyInt())).thenReturn(
      Optional.of(
        getOffenderAssessment_MinimalBuilder(-1L, 2, "NN123N", -11L)
          .assessmentItems(
            listOf(
              OffenderAssessmentItem.builder()
                .assessmentAnswer(
                  AssessmentEntry.builder()
                    .description("Answer without a question")
                    .parentAssessment(
                      AssessmentEntry.builder()
                        .assessmentId(-10L)
                        .build(),
                    )
                    .build(),
                )
                .build(),
            ),
          )
          .build(),
      ),
    )
    whenever(assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-11L)).thenReturn(
      listOf(
        AssessmentEntry.builder()
          .description("Question 1")
          .build(),
      ),
    )
    val assessment = service.getOffenderAssessment(-1L, 2)
    assertThat(assessment.questions).hasSize(1).contains(
      AssessmentQuestion.builder()
        .question("Question 1")
        .build(),
    )
  }

  @Test
  fun offenderAssessment_handlesMinimalNonNullValues() {
    whenever(repository.findByBookingIdAndAssessmentSeq(anyLong(), anyInt())).thenReturn(
      Optional.of(
        getOffenderAssessment_MinimalBuilder(-1L, 2, "NN123N", -11L)
          .build(),
      ),
    )
    whenever(assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-11L)).thenReturn(
      listOf(
        AssessmentEntry.builder()
          .description("Question 2")
          .assessmentId(-9L)
          .build(),
      ),
    )
    service.getOffenderAssessment(-1L, 2)
  }

  @Test
  fun offenderAssessments_returnsCorrectApiObject() {
    whenever(repository.findWithDetailsByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("N1234AA"), "Y")).thenReturn(

      listOf(
        OffenderAssessment.builder()
          .bookingId(-1L)
          .assessmentSeq(2)
          .offenderBooking(
            OffenderBooking.builder()
              .offender(
                Offender.builder()
                  .nomsId("NN123N")
                  .build(),
              )
              .build(),
          )
          .assessmentType(
            AssessmentEntry.builder()
              .assessmentId(-11L)
              .assessmentCode("CSRREV1")
              .build(),
          )
          .assessmentItems(
            listOf(
              OffenderAssessmentItem.builder()
                .assessmentAnswer(
                  AssessmentEntry.builder()
                    .description("Answer 1")
                    .parentAssessment(
                      AssessmentEntry.builder()
                        .assessmentId(-10L)
                        .build(),
                    )
                    .build(),
                )
                .build(),
            ),
          )
          .calculatedClassification(AssessmentClassification("STANDARD", "Standard"))
          .overridingClassification(AssessmentClassification("HI", "High"))
          .reviewedClassification(AssessmentClassification("HI", "High"))
          .assessmentDate(LocalDate.parse("2019-01-02"))
          .assessmentCreateLocation(
            AgencyLocation.builder()
              .id("LEI")
              .build(),
          )
          .assessmentComment("Assessment Comment 1")
          .assessCommittee(AssessmentCommittee("RECP", "Reception"))
          .creationUser(
            StaffUserAccount.builder()
              .username("JBRIEN")
              .build(),
          )
          .evaluationDate(LocalDate.parse("2019-01-03"))
          .overrideReason(AssessmentOverrideReason("OVERRIDE_DUMMY_VALUE", "Review reason"))
          .reviewCommittee(AssessmentCommittee("REVW", "Review board"))
          .nextReviewDate(LocalDate.parse("2020-01-02"))
          .build(),
      ),
    )
    val assessmentApiObjects = service.getOffenderAssessments("N1234AA")
    assertThat(assessmentApiObjects).isEqualTo(
      listOf(
        AssessmentSummary.builder()
          .bookingId(-1L)
          .assessmentSeq(2)
          .offenderNo("NN123N")
          .classificationCode("HI")
          .assessmentCode("CSRREV1")
          .cellSharingAlertFlag(true)
          .assessmentDate(LocalDate.parse("2019-01-02"))
          .assessmentAgencyId("LEI")
          .assessmentComment("Assessment Comment 1")
          .assessorUser("JBRIEN")
          .nextReviewDate(LocalDate.parse("2020-01-02"))
          .build(),
      ),
    )
  }

  @Test
  fun offenderAssessments_returnsAllObjects() {
    whenever(repository.findWithDetailsByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("N1234AA"), "Y")).thenReturn(
      listOf(
        getOffenderAssessment_MinimalBuilder(-1L, 1, "N1234AA", -11L)
          .build(),
        getOffenderAssessment_MinimalBuilder(-1L, 2, "N1234AA", -11L)
          .build(),
      ),
    )
    val assessmentApiObjects = service.getOffenderAssessments("N1234AA")
    assertThat(assessmentApiObjects).isEqualTo(
      listOf(
        AssessmentSummary.builder()
          .bookingId(-1L)
          .assessmentSeq(1)
          .offenderNo("N1234AA")
          .cellSharingAlertFlag(true)
          .build(),
        AssessmentSummary.builder()
          .bookingId(-1L)
          .assessmentSeq(2)
          .offenderNo("N1234AA")
          .cellSharingAlertFlag(true)
          .build(),
      ),
    )
  }

  @Test
  fun currentCsraClassification_returnsResultsOfFirstAssessmentIfSet() {
    whenever(repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("N1234AA"))).thenReturn(
      listOf(
        getOffenderAssessment_CsraClassificationBuilder("N1234AA", AssessmentClassification("HI", "High"), LocalDate.parse("2019-01-02"))
          .build(),
        getOffenderAssessment_CsraClassificationBuilder("N1234AA", AssessmentClassification("STANDARD", "Standard"), LocalDate.parse("2019-01-01"))
          .build(),
      ),
    )
    val csraClassificationCode = service.getCurrentCsraClassification("N1234AA")
    assertThat(csraClassificationCode.classificationCode).isEqualTo("HI")
    assertThat(csraClassificationCode.classificationDate).isEqualTo(LocalDate.parse("2019-01-02"))
  }

  @Test
  fun currentCsraClassification_returnsResultsOfNextAssessmentIfFirstNotSet() {
    whenever(repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("N1234AA"))).thenReturn(
      listOf(
        getOffenderAssessment_CsraClassificationBuilder("N1234AA", null, LocalDate.parse("2019-01-03"))
          .build(),
        getOffenderAssessment_CsraClassificationBuilder("N1234AA", AssessmentClassification("STANDARD", "Standard"), LocalDate.parse("2019-01-02"))
          .build(),
        getOffenderAssessment_CsraClassificationBuilder("N1234AA", AssessmentClassification("HI", "High"), LocalDate.parse("2019-01-01"))
          .build(),
      ),
    )
    val csraClassificationCode = service.getCurrentCsraClassification("N1234AA")
    assertThat(csraClassificationCode.classificationCode).isEqualTo("STANDARD")
    assertThat(csraClassificationCode.classificationDate).isEqualTo(LocalDate.parse("2019-01-02"))
  }

  @Test
  fun currentCsraClassification_returnsNullIfNoAssessmentsWithResults() {
    whenever(repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("N1234AA"))).thenReturn(
      listOf(
        getOffenderAssessment_CsraClassificationBuilder("N1234AA", null, LocalDate.parse("2019-01-02"))
          .build(),
      ),
    )
    val csraClassificationCode = service.getCurrentCsraClassification("N1234AA")
    assertThat(csraClassificationCode).isNull()
  }

  @Test
  fun currentCsraClassification_returnsNullIfNoAssessments() {
    whenever(repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("N1234AA"))).thenReturn(listOf())
    val csraClassificationCode = service.getCurrentCsraClassification("N1234AA")
    assertThat(csraClassificationCode).isNull()
  }

  @Test
  fun offendersAssessmentRatings_returnsResultsOfNextAssessmentIfLatestNotFinalised() {
    whenever(repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("N1234AA"))).thenReturn(
      listOf(
        getOffenderAssessment_CsraClassificationBuilder("N1234AA", null, LocalDate.parse("2019-01-03"))
          .build(),
        getOffenderAssessment_CsraClassificationBuilder("N1234AA", AssessmentClassification("HI", "High"), LocalDate.parse("2019-01-02"))
          .build(),
      ),
    )
    val csraRatings = service.getOffendersAssessmentRatings(listOf("N1234AA"))
    assertThat(csraRatings).isEqualTo(
      listOf(
        uk.gov.justice.hmpps.prison.api.model.AssessmentClassification.builder()
          .offenderNo("N1234AA")
          .classificationCode("HI")
          .classificationDate(LocalDate.parse("2019-01-02"))
          .build(),
      ),
    )
  }

  @Test
  fun offendersAssessmentRatings_returnsOnlyOffendersRequestedWithAssessments() {
    whenever(repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("N1234AA", "N2345BB"))).thenReturn(
      listOf(
        getOffenderAssessment_CsraClassificationBuilder("N1234AA", AssessmentClassification("HI", "High"), LocalDate.parse("2019-01-02"))
          .build(),
      ),
    )
    val csraRatings = service.getOffendersAssessmentRatings(listOf("N1234AA", "N2345BB"))
    assertThat(csraRatings).isEqualTo(
      listOf(
        uk.gov.justice.hmpps.prison.api.model.AssessmentClassification.builder()
          .offenderNo("N1234AA")
          .classificationCode("HI")
          .classificationDate(LocalDate.parse("2019-01-02"))
          .build(),
      ),
    )
  }

  @Test
  fun offendersAssessmentRatings_returnsBatchedAssessments() {
    val serviceWithSmallBatchSize = OffenderAssessmentService(repository, assessmentRepository, 2)
    whenever(repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("N1234AA", "N2345BB"))).thenReturn(
      listOf(
        getOffenderAssessment_CsraClassificationBuilder("N1234AA", AssessmentClassification("HI", "High"), LocalDate.parse("2019-01-02"))
          .build(),
      ),
    )
    whenever(repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(listOf("N3456CC"))).thenReturn(
      listOf(
        getOffenderAssessment_CsraClassificationBuilder("N3456CC", AssessmentClassification("STANDARD", "Standard"), LocalDate.parse("2019-01-03"))
          .build(),
      ),
    )
    val csraRatings = serviceWithSmallBatchSize.getOffendersAssessmentRatings(listOf("N1234AA", "N2345BB", "N3456CC"))
    assertThat(csraRatings).isEqualTo(
      listOf(
        uk.gov.justice.hmpps.prison.api.model.AssessmentClassification.builder()
          .offenderNo("N1234AA")
          .classificationCode("HI")
          .classificationDate(LocalDate.parse("2019-01-02"))
          .build(),
        uk.gov.justice.hmpps.prison.api.model.AssessmentClassification.builder()
          .offenderNo("N3456CC")
          .classificationCode("STANDARD")
          .classificationDate(LocalDate.parse("2019-01-03"))
          .build(),
      ),
    )
  }

  private fun getOffenderAssessment_MinimalBuilder(
    bookingId: Long,
    assessmentSeq: Int,
    nomsId: String,
    assesmentTypeId: Long,
  ): OffenderAssessment.OffenderAssessmentBuilder {
    return OffenderAssessment.builder()
      .bookingId(bookingId)
      .assessmentSeq(assessmentSeq)
      .offenderBooking(
        OffenderBooking.builder()
          .offender(
            Offender.builder()
              .nomsId(nomsId)
              .build(),
          )
          .build(),
      )
      .assessmentType(
        AssessmentEntry.builder()
          .assessmentId(assesmentTypeId)
          .build(),
      )
      .assessmentItems(listOf())
  }

  private fun getOffenderAssessment_CsraClassificationBuilder(offenderNo: String, csraClassification: AssessmentClassification?, assessmentDate: LocalDate): OffenderAssessment.OffenderAssessmentBuilder {
    return OffenderAssessment.builder()
      .bookingId(-1L)
      .assessmentSeq(2)
      .assessmentDate(assessmentDate)
      .offenderBooking(
        OffenderBooking.builder()
          .offender(
            Offender.builder()
              .nomsId(offenderNo)
              .build(),
          )
          .build(),
      )
      .assessmentType(
        AssessmentEntry.builder()
          .assessmentId(-5L)
          .build(),
      )
      .assessmentItems(listOf())
      .reviewedClassification(csraClassification)
  }
}
