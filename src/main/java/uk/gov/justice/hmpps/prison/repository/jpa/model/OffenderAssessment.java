package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@NamedEntityGraph(
    name = "offender-assessment-with-details",
    attributeNodes = {
        @NamedAttributeNode(value = "offenderBooking", subgraph = "booking-details"),
        @NamedAttributeNode("assessCommittee"),
        @NamedAttributeNode("assessmentType"),
        @NamedAttributeNode("calculatedClassification"),
        @NamedAttributeNode("overridingClassification"),
        @NamedAttributeNode("overrideReason"),
        @NamedAttributeNode("reviewedClassification"),
        @NamedAttributeNode("reviewCommittee"),
    },
    subgraphs = {
        @NamedSubgraph(
            name = "booking-details",
            attributeNodes = {
                @NamedAttributeNode("offender"),
            }
        )
    }
)

public class OffenderAssessment extends AuditableEntity {

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

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AssessmentClassification.ASSESS_CLASS + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "CALC_SUP_LEVEL_TYPE", referencedColumnName = "code"))
    })
    private AssessmentClassification calculatedClassification;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AssessmentClassification.ASSESS_CLASS + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "OVERRIDED_SUP_LEVEL_TYPE", referencedColumnName = "code"))
    })
    private AssessmentClassification overridingClassification;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AssessmentClassification.ASSESS_CLASS + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "REVIEW_SUP_LEVEL_TYPE", referencedColumnName = "code"))
    })
    private AssessmentClassification reviewedClassification;

    @Column(name = "ASSESSMENT_DATE")
    private LocalDate assessmentDate;

    @Column(name = "ASSESS_COMMENT_TEXT")
    private String assessmentComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSESSMENT_CREATE_LOCATION")
    private AgencyLocation assessmentCreateLocation;

    @Column(name = "ASSESS_STATUS")
    private String assessStatus;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AssessmentCommittee.ASSESS_COMMITTEE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "ASSESS_COMMITTE_CODE", referencedColumnName = "code"))
    })
    private AssessmentCommittee assessCommittee;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AssessmentOverrideReason.OVERRIDE_REASON + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "OVERRIDE_REASON", referencedColumnName = "code"))
    })
    private AssessmentOverrideReason overrideReason;

    @ManyToOne
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

    public String getOffenderNo() {
        return getOffenderBooking().getOffender().getNomsId();
    }

    /**
     * Method to generate a summary of the classification outcome.
     */
    @NonNull
    public ClassificationSummary getClassificationSummary() {
        if (reviewedClassification != null && !reviewedClassification.isPending()) {
            return new ClassificationSummary(reviewedClassification, getPreviousClassification(), getApprovalReason());
        }
        if (calculatedClassification != null && !calculatedClassification.isPending() &&
                overridingClassification == null) {
            return new ClassificationSummary(calculatedClassification, null, null);
        }
        return ClassificationSummary.withoutClassification();
    }

    private AssessmentClassification getPreviousClassification() {
        if (calculatedClassification != null && !calculatedClassification.isPending() &&
            !calculatedClassification.equals(reviewedClassification)) {
            return calculatedClassification;
        }
        return null;
    }

    private String getApprovalReason() {
        var approvalReason = reviewCommitteeComment;
        if (!reviewedClassification.equals(calculatedClassification) && overrideReason != null) {
            return overrideReason.getDescription();
        }
        return approvalReason;
    }

    @Data
    @AllArgsConstructor
    public static class ClassificationSummary {
        private final AssessmentClassification finalClassification;
        private final AssessmentClassification originalClassification;
        private final String classificationApprovalReason;

        private static ClassificationSummary withoutClassification() {
            return new ClassificationSummary(null, null, null);
        }

        public boolean isSet() {
            return finalClassification != null;
        }
    }
}
