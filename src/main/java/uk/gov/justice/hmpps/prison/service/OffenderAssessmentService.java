package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.AssessmentClassification;
import uk.gov.justice.hmpps.prison.api.model.AssessmentDetail;
import uk.gov.justice.hmpps.prison.api.model.AssessmentQuestion;
import uk.gov.justice.hmpps.prison.api.model.AssessmentSummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessmentItem;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AssessmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAssessmentRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
@Validated
@Slf4j
public class OffenderAssessmentService {
    private final OffenderAssessmentRepository repository;
    private final AssessmentRepository assessmentRepository;
    private final int maxBatchSize;

    public OffenderAssessmentService(final OffenderAssessmentRepository repository,
                                     final AssessmentRepository assessmentRepository,
                                     @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.repository = repository;
        this.assessmentRepository = assessmentRepository;
        this.maxBatchSize = maxBatchSize;
    }

    @Transactional(readOnly = true)
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
            .overridingClassificationCode((assessmentDetails.getOverridingClassification() !=null)?assessmentDetails.getOverridingClassification().getCode():null)
            .calculatedClassificationCode((assessmentDetails.getCalculatedClassification() !=null)?assessmentDetails.getCalculatedClassification().getCode():null)
            .approvedClassificationCode((assessmentDetails.getReviewedClassification() !=null)?assessmentDetails.getReviewedClassification().getCode():null)
            .approvalComment((assessmentDetails.getReviewCommitteeComment() !=null)?assessmentDetails.getReviewCommitteeComment():null)
            .overrideReason((assessmentDetails.getOverrideReason() !=null)?assessmentDetails.getOverrideReason().getDescription():null)
            .build();
    }

    @Transactional(readOnly = true)
    public List<AssessmentSummary> getOffenderAssessments(final String offenderNo) {
        final var assessments = repository.findWithDetailsByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(List.of(offenderNo), "Y");

        return assessments.stream().map(this::getAssessmentSummary).collect(toList());
    }

    public CurrentCsraAssessment getCurrentCsraClassification(final String offenderNo) {
        final var assessments = repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(List.of(offenderNo), "Y");

        return calculateCurrentCsraClassification(assessments);
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

    private List<AssessmentQuestion> getCsraAssessmentQuestionsAndAnswers(final OffenderAssessment assessmentDetails, final Long bookingId, final Integer assessmentSeq) {
        final var assessmentQuestions = assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(assessmentDetails.getAssessmentType().getAssessmentId());

        if (assessmentQuestions.isEmpty()) {
            throw new EntityNotFoundException(String.format("Csra assessment questions for booking %s and sequence %s not found.", bookingId, assessmentSeq));
        }

        final var assessmentItems = assessmentDetails.getAssessmentItems();
        final var assessmentAnswersByQuestionId = assessmentItems.stream()
            .map(OffenderAssessmentItem::getAssessmentAnswer)
            .collect(groupingBy((aa) -> aa.getParentAssessment().getAssessmentId(), mapping(AssessmentEntry::getDescription, toList())));

        return assessmentQuestions.stream().map(aq -> getAssessmentQuestionAndAnswers(aq, assessmentAnswersByQuestionId.get(aq.getAssessmentId()))).collect(toList());
    }

    private AssessmentQuestion getAssessmentQuestionAndAnswers(final AssessmentEntry assessment, final List<String> answers) {
        if (answers == null) {
            return new AssessmentQuestion(assessment.getDescription(), null, null);
        }
        return new AssessmentQuestion(assessment.getDescription(),
            answers.stream().findFirst().orElse(null),
            answers.stream().skip(1).collect(toList()));
    }

    @Transactional(readOnly = true)
    public List<AssessmentClassification> getOffendersAssessmentRatings(final List<String> offenderNos) {
        final var batch = Lists.partition(new ArrayList<>(offenderNos), maxBatchSize);
        return batch.stream().flatMap(offenderNosBatch ->
            getOffendersCurrentAssessmentRating(offenderNosBatch).stream()
        ).collect(toList());
    }

    private List<AssessmentClassification> getOffendersCurrentAssessmentRating(final List<String> offenderNos) {
        final var assessmentsLatestFirstForAllOffenders = repository.findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(offenderNos, "Y");
        final var assessmentsLatestFirstByOffenderNo = assessmentsLatestFirstForAllOffenders.stream().collect(groupingBy(OffenderAssessment::getOffenderNo));
        return assessmentsLatestFirstByOffenderNo.entrySet().stream()
            .map(e -> getOffenderCurrentAssessmentRating(e.getKey(), e.getValue()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());
    }

    private Optional<AssessmentClassification> getOffenderCurrentAssessmentRating(final String offenderNo, final List<OffenderAssessment> assessmentsLatestFirst) {
        final var currentClassification = calculateCurrentCsraClassification(assessmentsLatestFirst);
        if (currentClassification == null) {
            return Optional.empty();
        }
        return Optional.of(AssessmentClassification.builder()
            .offenderNo(offenderNo)
            .classificationCode(currentClassification.classificationCode)
            .classificationDate(currentClassification.classificationDate)
            .build());
    }

    private CurrentCsraAssessment calculateCurrentCsraClassification(final List<OffenderAssessment> offenderAssessmentsLatestFirst) {
        return offenderAssessmentsLatestFirst.stream().filter(a -> a.getClassificationSummary().isSet()).findFirst()
            .map(CurrentCsraAssessment::fromAssessment).orElse(null);
    }

    @AllArgsConstructor
    @Data
    public static class CurrentCsraAssessment {
        private String classificationCode;
        private LocalDate classificationDate;

        public static CurrentCsraAssessment fromAssessment(OffenderAssessment assessmentWithClassificationSet) {
            return new CurrentCsraAssessment(assessmentWithClassificationSet.getClassificationSummary().getFinalClassification().getCode(),
                assessmentWithClassificationSet.getAssessmentDate());
        }
    }
}
