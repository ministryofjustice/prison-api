package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;
import lombok.Builder.Default;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction.MLTY_DISCP;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch.MLTY_BRANCH;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge.MLTY_DSCHRG;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank.MLTY_RANK;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone.MLTY_WZONE;

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

    @OneToOne(fetch = FetchType.LAZY)
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
