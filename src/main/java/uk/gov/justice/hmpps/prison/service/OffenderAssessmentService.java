package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.AssessmentDetail;
import uk.gov.justice.hmpps.prison.api.model.AssessmentQuestion;
import uk.gov.justice.hmpps.prison.api.model.AssessmentSummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessmentItem;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AssessmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAssessmentRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import java.util.List;
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
    public AssessmentDetail getOffenderAssessment(final Long bookingId, final Integer assessmentSeq) {
        final var assessment = repository.findByBookingIdAndAssessmentSeq(bookingId, assessmentSeq);

        if (assessment.isEmpty()) {
            throw new EntityNotFoundException(String.format("Csra assessment for booking %s and sequence %s not found.", bookingId, assessmentSeq));
        }

        final var assessmentDetails = assessment.get();
        final var classificationSummary = assessmentDetails.getClassificationSummary();

        return AssessmentDetail.detailBuilder()
            .summary(getAssessmentSummary(assessmentDetails))
            .assessmentCommitteeCode((assessmentDetails.getAssessCommittee() != null)?assessmentDetails.getAssessCommittee().getCode():null)
            .assessmentCommitteeName((assessmentDetails.getAssessCommittee() != null)?assessmentDetails.getAssessCommittee().getDescription():null)
            .approvalDate(assessmentDetails.getEvaluationDate())
            .approvalCommitteeCode((assessmentDetails.getReviewCommittee() != null)?assessmentDetails.getReviewCommittee().getCode():null)
            .approvalCommitteeName((assessmentDetails.getReviewCommittee() != null)?assessmentDetails.getReviewCommittee().getDescription():null)
            .originalClassificationCode((classificationSummary.getOriginalClassification() != null)?classificationSummary.getOriginalClassification().getCode(): null)
            .classificationReviewReason(classificationSummary.getClassificationApprovalReason())
            .questions(getCsraAssessmentQuestionsAndAnswers(assessmentDetails, bookingId, assessmentSeq))
            .build();
    }

    @Transactional(readOnly = true)
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<AssessmentSummary> getOffenderAssessments(final String offenderNo) {
        final var assessments = repository.findByCsraAssessmentAndByOffenderNo(offenderNo);

        return assessments.stream().map(this::getAssessmentSummary).collect(Collectors.toList());
    }

    private AssessmentSummary getAssessmentSummary(final OffenderAssessment assessmentDetails) {
        final var classificationSummary = assessmentDetails.getClassificationSummary();

        return AssessmentSummary.builder()
            .bookingId(assessmentDetails.getBookingId())
            .assessmentSeq(assessmentDetails.getAssessmentSeq())
            .offenderNo(assessmentDetails.getOffenderBooking().getOffender().getNomsId())
            .classificationCode((classificationSummary.getFinalClassification() != null)?classificationSummary.getFinalClassification().getCode(): null)
            .assessmentCode(assessmentDetails.getAssessmentType().getAssessmentCode())
            .cellSharingAlertFlag(true)
            .assessmentDate(assessmentDetails.getAssessmentDate())
            .assessmentAgencyId((assessmentDetails.getAssessmentCreateLocation() != null)?assessmentDetails.getAssessmentCreateLocation().getId(): null)
            .assessmentComment(assessmentDetails.getAssessmentComment())
            .assessorUser((assessmentDetails.getCreationUser() != null)?assessmentDetails.getCreationUser().getUsername():null)
            .nextReviewDate(assessmentDetails.getNextReviewDate())
            .build();
    }

    public String getCsraClassificationCode(final String offenderNo) {
        final var assessments = repository.findByCsraAssessmentAndByOffenderNo(offenderNo);

        return assessments.stream().filter(a -> a.getClassificationSummary().isSet()).findFirst()
            .map(a -> a.getClassificationSummary().getFinalClassification().getCode()).orElse(null);
    }

    private List<AssessmentQuestion> getCsraAssessmentQuestionsAndAnswers(final OffenderAssessment assessmentDetails, final Long bookingId, final Integer assessmentSeq) {
        final var assessmentQuestions = assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(assessmentDetails.getAssessmentType().getAssessmentId());

        if (assessmentQuestions.isEmpty()) {
            throw new EntityNotFoundException(String.format("Csra assessment questions for booking %s and sequence %s not found.", bookingId, assessmentSeq));
        }

        final var assessmentItems = assessmentDetails.getAssessmentItems();
        final var assessmentAnswersByQuestionId = assessmentItems.stream().map(OffenderAssessmentItem::getAssessmentAnswer).collect(Collectors.toMap((aa) -> aa.getParentAssessment().getAssessmentId(), AssessmentEntry::getDescription));

        return assessmentQuestions.stream().map(aq -> new AssessmentQuestion(aq.getDescription(), assessmentAnswersByQuestionId.get(aq.getAssessmentId()))).collect(Collectors.toList());
    }
}
