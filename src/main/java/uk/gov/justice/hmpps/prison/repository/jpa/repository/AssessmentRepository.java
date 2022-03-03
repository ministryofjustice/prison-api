package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.api.model.Assessment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment;

import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends CrudRepository<AssessmentEntry, Long> {
    @Query("SELECT question FROM AssessmentEntry question " +
        "INNER JOIN question.parentAssessment questionSet INNER JOIN questionSet.parentAssessment type " +
        "WHERE type.assessmentId = :assessmentId AND type.parentAssessment is null " +
        "AND type.cellSharingAlertFlag = 'Y' AND questionSet.assessmentCode != 'COMPLETE' " +
        "ORDER BY question.listSeq ASC")
    List<AssessmentEntry> findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(Long assessmentId);
}
