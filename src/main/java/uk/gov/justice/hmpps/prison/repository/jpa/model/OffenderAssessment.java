package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.springframework.lang.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderAssessment.Pk.class)
@Table(name = "OFFENDER_ASSESSMENTS")
@ToString(of = {"bookingId", "assessmentSeq"})
public class OffenderAssessment extends ExtendedAuditableEntity {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Id
    @Column(name = "ASSESSMENT_SEQ")
    private Integer assessmentSeq;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    @MapsId("bookingId")
    private OffenderBooking offenderBooking;

    @Column(name = "CALC_SUP_LEVEL_TYPE")
    private String calculatedClassification;

    @Column(name = "OVERRIDED_SUP_LEVEL_TYPE")
    private String overridingClassification;

    @Column(name = "REVIEW_SUP_LEVEL_TYPE")
    private String reviewedClassification;

    @Column(name = "ASSESSMENT_DATE")
    private LocalDate assessmentDate;

    @Column(name = "ASSESS_COMMENT_TEXT")
    private String assessmentComment;

    @Column(name = "ASSESSMENT_CREATE_LOCATION")
    private String assessmentCreateLocation;

    @Column(name = "ASSESS_STATUS")
    private String assessStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AssessmentCommittee.ASSESS_COMMITTEE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "ASSESS_COMMITTE_CODE", referencedColumnName = "code"))
    })
    private AssessmentCommittee assessCommittee;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AssessmentOverrideReason.OVERRIDE_REASON + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "OVERRIDE_REASON", referencedColumnName = "code"))
    })
    private AssessmentOverrideReason overrideReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AssessmentCommittee.ASSESS_COMMITTEE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "REVIEW_COMMITTE_CODE", referencedColumnName = "code"))
    })
    private AssessmentCommittee reviewCommittee;

    @Column(name = "COMMITTE_COMMENT_TEXT")
    private String reviewCommitteeComment;

    @Column(name = "NEXT_REVIEW_DATE")
    private LocalDate nextReviewDate;

    @Column(name = "EVALUATION_DATE")
    private LocalDate evaluationDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CREATION_USER")
    private StaffUserAccount creationUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ASSESSMENT_TYPE_ID")
    private AssessmentEntry assessmentType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="OFFENDER_BOOK_ID", referencedColumnName="OFFENDER_BOOK_ID"),
        @JoinColumn(name="ASSESSMENT_SEQ", referencedColumnName="ASSESSMENT_SEQ")
    })
    private List<OffenderAssessmentItem> assessmentItems;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Pk implements Serializable {
        private Long bookingId;
        private Integer assessmentSeq;
    }

    /**
     * Method to generate a summary of the classification outcome.
     */
    @NonNull
    public ClassificationSummary getClassificationSummary() {
        if (reviewedClassification != null && !reviewedClassification.equals("PEND")) {
            String previousClassification = null;
            if (calculatedClassification != null && !calculatedClassification.equals("PEND") &&
                    !calculatedClassification.equals(reviewedClassification)) {
                previousClassification = calculatedClassification;
            }
            var approvalReason = reviewCommitteeComment;
            if (!reviewedClassification.equals(calculatedClassification) && overrideReason != null) {
                approvalReason = overrideReason.getDescription();
            }
            return new ClassificationSummary(reviewedClassification, previousClassification, approvalReason);
        }
        if (calculatedClassification != null && !calculatedClassification.equals("PEND")) {
            return new ClassificationSummary(calculatedClassification, null, null);
        }
        return new ClassificationSummary(null, null, null);
    }

    @Data
    @AllArgsConstructor
    public static class ClassificationSummary {
        private final String finalClassification;
        private final String originalClassification;
        private final String classificationApprovalReason;
    }
}
