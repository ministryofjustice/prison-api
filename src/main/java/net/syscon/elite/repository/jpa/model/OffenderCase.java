package net.syscon.elite.repository.jpa.model;

import lombok.*;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.*;
import java.time.LocalDateTime;

import static net.syscon.elite.repository.jpa.model.ReferenceCode.CASE_STATUS;
import static net.syscon.elite.repository.jpa.model.ReferenceCode.LEG_CASE_TYP;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "OFFENDER_CASES")
public class OffenderCase extends AuditableEntity {

    @Id
    @Column(name = "CASE_ID", nullable = false)
    private Long id;

    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Column(name = "CASE_SEQ")
    private Long caseSeq;

    @Column(name = "BEGIN_DATE")
    private LocalDateTime beginDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "AGY_LOC_ID")
    private AgencyLocation agencyLocation;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + LEG_CASE_TYP + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "CASE_TYPE", referencedColumnName = "code"))
    })
    private LegalCaseType legalCaseType;

    @Column(name = "CASE_INFO_PREFIX")
    private String caseInfoPrefix;

    @Column(name = "CASE_INFO_NUMBER")

    private String caseInfoNumber;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CASE_STATUS + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "CASE_STATUS", referencedColumnName = "code"))
    })
    private CaseStatus caseStatus;

    @Column(name = "COMBINED_CASE_ID")
    private Long combinedCaseId;
}
