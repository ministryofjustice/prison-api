package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ASSESSMENTS")
@ToString
public class AssessmentEntry extends AuditableEntity implements Serializable {

    @Id
    @Column(name = "ASSESSMENT_ID")
    private Long assessmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PARENT_ASSESSMENT_ID")
    private AssessmentEntry parentAssessment;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "LIST_SEQ")
    private Long listSeq;

    @Column(name = "ASSESSMENT_CODE")
    private String assessmentCode;

    @Column(name = "CELL_SHARING_ALERT_FLAG")
    private String cellSharingAlertFlag;
}
