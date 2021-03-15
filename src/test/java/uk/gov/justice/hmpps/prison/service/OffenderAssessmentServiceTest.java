package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.AssessmentDetail;
import uk.gov.justice.hmpps.prison.api.model.AssessmentQuestion;
import uk.gov.justice.hmpps.prison.api.model.AssessmentSummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentClassification;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentCommittee;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentOverrideReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment.OffenderAssessmentBuilder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessmentItem;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AssessmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAssessmentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderAssessmentServiceTest {
    @Mock
    private OffenderAssessmentRepository repository;

    @Mock
    private AssessmentRepository assessmentRepository;

    private OffenderAssessmentService service;

    @BeforeEach
    public void beforeEach() {
        service = new OffenderAssessmentService(repository, assessmentRepository);
    }

    @Test
    public void getOffenderAssessment_returnsCorrectApiObject() {
        when(repository.findByBookingIdAndAssessmentSeq(-1L, 2)).thenReturn(Optional.of(
            OffenderAssessment.builder()
                .bookingId(-1L)
                .assessmentSeq(2)
                .offenderBooking(OffenderBooking.builder()
                    .offender(Offender.builder()
                        .nomsId("NN123N")
                        .build())
                    .build())
                .assessmentType(AssessmentEntry.builder()
                    .assessmentId(-11L)
                    .assessmentCode("CSRREV1")
                    .build())
                .assessmentItems(List.of(
                    OffenderAssessmentItem.builder()
                        .assessmentAnswer(AssessmentEntry.builder()
                            .description("Answer 1")
                            .parentAssessment(AssessmentEntry.builder()
                                .assessmentId(-10L)
                                .build())
                            .build())
                        .build()
                ))
                .calculatedClassification(new AssessmentClassification("STANDARD", "Standard"))
                .overridingClassification(new AssessmentClassification("HI", "High"))
                .reviewedClassification(new AssessmentClassification("HI", "High"))
                .assessmentDate(LocalDate.parse("2019-01-02"))
                .assessmentCreateLocation(AgencyLocation.builder()
                    .id("LEI")
                    .build())
                .assessmentComment("Assessment Comment 1")
                .assessCommittee(new AssessmentCommittee("RECP", "Reception"))
                .creationUser(StaffUserAccount.builder()
                    .username("JBRIEN")
                    .build())
                .evaluationDate(LocalDate.parse("2019-01-03"))
                .overrideReason(new AssessmentOverrideReason("OVERRIDE_DUMMY_VALUE", "Review reason"))
                .reviewCommittee(new AssessmentCommittee("REVW", "Review board"))
                .nextReviewDate(LocalDate.parse("2020-01-02"))
                .build()
        ));

        when(assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-11L)).thenReturn(List.of(
            AssessmentEntry.builder()
                .description("Question 1")
                .assessmentId(-10L)
                .build(),
            AssessmentEntry.builder()
                .description("Question 2")
                .assessmentId(-9L)
                .build()
        ));

        final var assessmentApiObject = service.getOffenderAssessment(-1L, 2);

        assertThat(assessmentApiObject).isEqualTo(AssessmentDetail.detailBuilder()
            .summary(AssessmentSummary.builder()
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
                .build())
            .assessmentCommitteeCode("RECP")
            .assessmentCommitteeName("Reception")
            .approvalDate(LocalDate.parse("2019-01-03"))
            .approvalCommitteeCode("REVW")
            .approvalCommitteeName("Review board")
            .originalClassificationCode("STANDARD")
            .classificationReviewReason("Review reason")
            .questions(List.of(
                AssessmentQuestion.builder()
                    .question("Question 1")
                    .answer("Answer 1")
                    .build(),
                AssessmentQuestion.builder()
                    .question("Question 2")
                    .build())
            )
            .build()
        );
    }

    @Test
    public void getOffenderAssessment_throwsEntityNotFoundIfNoMatch() {
        when(repository.findByBookingIdAndAssessmentSeq(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOffenderAssessment(-1L, 2)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getOffenderAssessment_throwsEntityNotFoundIfNoCsraQuestions() {
        when(repository.findByBookingIdAndAssessmentSeq(any(), any())).thenReturn(Optional.of(
            getOffenderAssessment_MinimalBuilder(-1L, 2, "NN123N", -11L)
            .build()
        ));

        when(assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-11L)).thenReturn(List.of(
        ));

        assertThatThrownBy(() -> service.getOffenderAssessment(-1L, 2)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getOffenderAssessment_ignoresAnswersThatAreNotInCsraQuestions() {
        when(repository.findByBookingIdAndAssessmentSeq(any(), any())).thenReturn(Optional.of(
            getOffenderAssessment_MinimalBuilder(-1L, 2, "NN123N", -11L)
                .assessmentItems(List.of(
                    OffenderAssessmentItem.builder()
                        .assessmentAnswer(AssessmentEntry.builder()
                            .description("Answer without a question")
                            .parentAssessment(AssessmentEntry.builder()
                                .assessmentId(-10L)
                                .build())
                            .build())
                        .build()
                ))
            .build()
        ));

        when(assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-11L)).thenReturn(List.of(
            AssessmentEntry.builder()
                .description("Question 1")
                .build()
        ));

        final var assessment = service.getOffenderAssessment(-1L, 2);
        assertThat(assessment.getQuestions()).hasSize(1).contains(AssessmentQuestion.builder()
            .question("Question 1")
            .build());
    }

    @Test
    public void getOffenderAssessment_handlesMinimalNonNullValues() {
        when(repository.findByBookingIdAndAssessmentSeq(any(), any())).thenReturn(Optional.of(
            getOffenderAssessment_MinimalBuilder(-1L, 2, "NN123N", -11L)
            .build()
        ));

        when(assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-11L)).thenReturn(List.of(
            AssessmentEntry.builder()
                .description("Question 2")
                .assessmentId(-9L)
                .build()
        ));

        service.getOffenderAssessment(-1L, 2);
    }

    @Test
    public void getOffenderAssessments_returnsCorrectApiObject() {
        when(repository.findByCsraAssessmentAndByOffenderNo_OrderByLatestFirst("N1234AA")).thenReturn(List.of(
            OffenderAssessment.builder()
                .bookingId(-1L)
                .assessmentSeq(2)
                .offenderBooking(OffenderBooking.builder()
                    .offender(Offender.builder()
                        .nomsId("NN123N")
                        .build())
                    .build())
                .assessmentType(AssessmentEntry.builder()
                    .assessmentId(-11L)
                    .assessmentCode("CSRREV1")
                    .build())
                .assessmentItems(List.of(
                    OffenderAssessmentItem.builder()
                        .assessmentAnswer(AssessmentEntry.builder()
                            .description("Answer 1")
                            .parentAssessment(AssessmentEntry.builder()
                                .assessmentId(-10L)
                                .build())
                            .build())
                        .build()
                ))
                .calculatedClassification(new AssessmentClassification("STANDARD", "Standard"))
                .overridingClassification(new AssessmentClassification("HI", "High"))
                .reviewedClassification(new AssessmentClassification("HI", "High"))
                .assessmentDate(LocalDate.parse("2019-01-02"))
                .assessmentCreateLocation(AgencyLocation.builder()
                    .id("LEI")
                    .build())
                .assessmentComment("Assessment Comment 1")
                .assessCommittee(new AssessmentCommittee("RECP", "Reception"))
                .creationUser(StaffUserAccount.builder()
                    .username("JBRIEN")
                    .build())
                .evaluationDate(LocalDate.parse("2019-01-03"))
                .overrideReason(new AssessmentOverrideReason("OVERRIDE_DUMMY_VALUE", "Review reason"))
                .reviewCommittee(new AssessmentCommittee("REVW", "Review board"))
                .nextReviewDate(LocalDate.parse("2020-01-02"))
                .build()
        ));

        final var assessmentApiObjects = service.getOffenderAssessments("N1234AA");

        assertThat(assessmentApiObjects).isEqualTo(List.of(
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
                .build()
            )
        );
    }

    @Test
    public void getOffenderAssessments_returnsAllObjects() {
        when(repository.findByCsraAssessmentAndByOffenderNo_OrderByLatestFirst("N1234AA")).thenReturn(List.of(
            getOffenderAssessment_MinimalBuilder(-1L, 1, "N1234AA", -11L)
                .build(),
            getOffenderAssessment_MinimalBuilder(-1L, 2, "N1234AA", -11L)
                .build()
        ));

        final var assessmentApiObjects = service.getOffenderAssessments("N1234AA");

        assertThat(assessmentApiObjects).isEqualTo(List.of(
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
                .build()
            )
        );
    }

    @Test
    public void getCurrentCsraClassification_returnsResultsOfFirstAssessmentIfSet() {
        when(repository.findByCsraAssessmentAndByOffenderNo_OrderByLatestFirst("N1234AA")).thenReturn(List.of(
            getOffenderAssessment_CsraClassificationBuilder(new AssessmentClassification("HI", "High"), LocalDate.parse("2019-01-02"))
                .build(),
            getOffenderAssessment_CsraClassificationBuilder(new AssessmentClassification("STANDARD", "Standard"), LocalDate.parse("2019-01-01"))
                .build()
        ));

        final var csraClassificationCode = service.getCurrentCsraClassification("N1234AA");

        assertThat(csraClassificationCode.getClassificationCode()).isEqualTo("HI");
        assertThat(csraClassificationCode.getClassificationDate()).isEqualTo(LocalDate.parse("2019-01-02"));
    }

    @Test
    public void getCurrentCsraClassification_returnsResultsOfNextAssessmentIfFirstNotSet() {
        when(repository.findByCsraAssessmentAndByOffenderNo_OrderByLatestFirst("N1234AA")).thenReturn(List.of(
            getOffenderAssessment_CsraClassificationBuilder(null, LocalDate.parse("2019-01-03"))
                .build(),
            getOffenderAssessment_CsraClassificationBuilder(new AssessmentClassification("STANDARD", "Standard"), LocalDate.parse("2019-01-02"))
                .build(),
            getOffenderAssessment_CsraClassificationBuilder(new AssessmentClassification("HI", "High"), LocalDate.parse("2019-01-01"))
                .build()
        ));

        final var csraClassificationCode = service.getCurrentCsraClassification("N1234AA");

        assertThat(csraClassificationCode.getClassificationCode()).isEqualTo("STANDARD");
        assertThat(csraClassificationCode.getClassificationDate()).isEqualTo(LocalDate.parse("2019-01-02"));
    }

    @Test
    public void getCurrentCsraClassification_returnsNullIfNoAssessmentsWithResults() {
        when(repository.findByCsraAssessmentAndByOffenderNo_OrderByLatestFirst("N1234AA")).thenReturn(List.of(
            getOffenderAssessment_CsraClassificationBuilder(null, LocalDate.parse("2019-01-02"))
                .build()
        ));

        final var csraClassificationCode = service.getCurrentCsraClassification("N1234AA");

        assertThat(csraClassificationCode).isEqualTo(null);
    }

    @Test
    public void getCurrentCsraClassification_returnsNullIfNoAssessments() {
        when(repository.findByCsraAssessmentAndByOffenderNo_OrderByLatestFirst("N1234AA")).thenReturn(List.of());

        final var csraClassificationCode = service.getCurrentCsraClassification("N1234AA");

        assertThat(csraClassificationCode).isEqualTo(null);
    }

    private OffenderAssessmentBuilder getOffenderAssessment_MinimalBuilder(final long bookingId, final int assessmentSeq,
                                                                           final String nomsId, final long assesmentTypeId) {
        return OffenderAssessment.builder()
            .bookingId(bookingId)
            .assessmentSeq(assessmentSeq)
            .offenderBooking(OffenderBooking.builder()
                .offender(Offender.builder()
                    .nomsId(nomsId)
                    .build())
                .build())
            .assessmentType(AssessmentEntry.builder()
                .assessmentId(assesmentTypeId)
                .build())
            .assessmentItems(List.of());
    }

    private OffenderAssessmentBuilder getOffenderAssessment_CsraClassificationBuilder(final AssessmentClassification csraClassification, final LocalDate assessmentDate) {
        return OffenderAssessment.builder()
            .bookingId(-1L)
            .assessmentSeq(2)
            .assessmentDate(assessmentDate)
            .offenderBooking(OffenderBooking.builder()
                .offender(Offender.builder()
                    .nomsId("NN123N")
                    .build())
                .build())
            .assessmentType(AssessmentEntry.builder()
                .assessmentId(-5L)
                .build())
            .assessmentItems(List.of())
            .reviewedClassification(csraClassification);
    }
}
