package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.AssessmentDetail;
import uk.gov.justice.hmpps.prison.api.model.AssessmentQuestion;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment;
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
                .calculatedClassification("STANDARD")
                .overridingClassification("HI")
                .reviewedClassification("HI")
                .assessmentDate(LocalDate.parse("2019-01-02"))
                .assessmentCreateLocation("LEI")
                .assessmentComment("Assessment Comment 1")
                .assessCommitteeCode("RECP")
                .creationUser(StaffUserAccount.builder()
                    .username("JBRIEN")
                    .build())
                .evaluationDate(LocalDate.parse("2019-01-03"))
                .overrideReason("Review reason")
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

        assertThat(assessmentApiObject).isEqualTo(AssessmentDetail.builder()
            .bookingId(-1L)
            .assessmentSeq(2)
            .offenderNo("NN123N")
            .classificationCode("HI")//
            .assessmentCode("CSRREV1")//
            .cellSharingAlertFlag(true)
            .assessmentDate(LocalDate.parse("2019-01-02"))
            .assessmentAgencyId("LEI")
            .assessmentComment("Assessment Comment 1")
            .assessmentCommitteeCode("RECP")
            .assessorUser("JBRIEN")
            .approvalDate(LocalDate.parse("2019-01-03"))
            .approvalUser(null) // TODO
            .originalClassificationCode("STANDARD")
            .classificationReviewReason("Review reason")
            .nextReviewDate(LocalDate.parse("2020-01-02"))
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
    public void getOffenderAssessment_throwsEntityNotFoundIfNotCsra() {
        when(repository.findByBookingIdAndAssessmentSeq(any(), any())).thenReturn(Optional.of(
            getOffenderAssessment_MinimalInput(-1L, 2, "NN123N", -11L)
        ));

        when(assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-11L)).thenReturn(List.of(
        ));

        assertThatThrownBy(() -> service.getOffenderAssessment(-1L, 2)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getOffenderAssessment_handlesMinimalNonNullValues() {
        when(repository.findByBookingIdAndAssessmentSeq(any(), any())).thenReturn(Optional.of(
            getOffenderAssessment_MinimalInput(-1L, 2, "NN123N", -11L)
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
    private OffenderAssessment getOffenderAssessment_MinimalInput(long bookingId, int assessmentSeq,
                                                                  String nomsId, long assesmentTypeId) {
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
                .assessmentItems(List.of())
                .build();
    }
}
