package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.AssessmentDetail;
import uk.gov.justice.hmpps.prison.api.model.AssessmentQuestion;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessmentItem;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AssessmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAssessmentRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.util.stream.Collectors;

@Service
@Validated
@Slf4j
public class OffenderAssessmentService {
    private final OffenderAssessmentRepository repository;
    private final AssessmentRepository assessmentRepository;

    public OffenderAssessmentService(final OffenderAssessmentRepository repository,
                                     final AssessmentRepository assessmentRepository) {
        this.repository = repository;
        this.assessmentRepository = assessmentRepository;
    }

    @Transactional(readOnly = true)
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public AssessmentDetail getOffenderAssessment(Long bookingId, Long assessmentSeq) {
        final var assessment = repository.findByBookingIdAndAssessmentSeq(bookingId, assessmentSeq);

        if (assessment.isEmpty()) {
            throw new EntityNotFoundException(String.format("Csra assessment for booking %s and sequence %s not found", bookingId, assessmentSeq));
        }

        final var assessmentDetails = assessment.get();
        final var assessmentQuestions = assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(assessmentDetails.getAssessmentType().getAssessmentId());

        if (assessmentQuestions.isEmpty()) {
            throw new EntityNotFoundException(String.format("Csra assessment questions for booking %s and sequence %s not found", bookingId, assessmentSeq));
        }

        final var assessmentItems = assessmentDetails.getAssessmentItems();
        final var assessmentAnswersByQuestionId = assessmentItems.stream().map(OffenderAssessmentItem::getAssessmentAnswer).collect(Collectors.toMap((aa) -> aa.getParentAssessment().getAssessmentId(), AssessmentEntry::getDescription));

        final var assessmentQuestionsAndAnswers = assessmentQuestions.stream().map(aq -> new AssessmentQuestion(aq.getDescription(), assessmentAnswersByQuestionId.get(aq.getAssessmentId()))).collect(Collectors.toList());

        final var classificationProcessor = assessmentDetails.getClassificationSummary();

        // TODO - Null checks
        return AssessmentDetail.builder()
            .bookingId(assessmentDetails.getBookingId())
            .assessmentSeq(assessmentDetails.getAssessmentSeq().intValue())
            .offenderNo(assessmentDetails.getOffenderBooking().getOffender().getNomsId())
            .classificationCode(classificationProcessor.getFinalClassification())
            .assessmentCode(assessmentDetails.getAssessmentType().getAssessmentCode())
            .cellSharingAlertFlag(true)
            .assessmentDate(assessmentDetails.getAssessmentDate())
            .assessmentAgencyId(assessmentDetails.getAssessmentCreateLocation())
            .assessmentComment(assessmentDetails.getAssessmentComment())
            .assessmentCommitteeCode(assessmentDetails.getAssessCommitteeCode())
            .assessorUser(assessmentDetails.getCreationUser())
            .approvalDate(assessmentDetails.getEvaluationDate())
            .approvalUser(assessmentDetails.getModifyUser())
            .originalClassificationCode(classificationProcessor.getOriginalClassification())
            .classificationReviewReason(assessmentDetails.getOverrideReason())
            .nextReviewDate(assessmentDetails.getNextReviewDate())
            .questions(assessmentQuestionsAndAnswers)
            .build();
    }
}
