package net.syscon.elite.repository.jpa.model;

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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static net.syscon.elite.repository.jpa.model.ReferenceCode.CASE_STS;
import static net.syscon.elite.repository.jpa.model.ReferenceCode.LEG_CASE_TYP;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "OFFENDER_CASES")
@ToString(exclude = "offenderBooking")
public class OffenderCourtCase extends AuditableEntity {

    @Id
    @Column(name = "CASE_ID", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "CASE_SEQ", nullable = false)
    private Long caseSeq;

    private LocalDate beginDate;

    @ManyToOne
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    private AgencyLocation agencyLocation;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + LEG_CASE_TYP + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "CASE_TYPE", referencedColumnName = "code"))
    })
    private LegalCaseType legalCaseType;

    private String caseInfoPrefix;

    private String caseInfoNumber;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CASE_STS + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "CASE_STATUS", referencedColumnName = "code"))
    })
    private CaseStatus caseStatus;

    @OneToOne
    @JoinColumn(name = "COMBINED_CASE_ID")
    private OffenderCourtCase combinedCase;

    @OneToMany(mappedBy = "offenderCourtCase", cascade = CascadeType.ALL)
    private List<CourtEvent> courtEvents;

    public Optional<LegalCaseType> getLegalCaseType() {
        return Optional.ofNullable(legalCaseType);
    }

    public Optional<CaseStatus> getCaseStatus() {
        return Optional.ofNullable(caseStatus);
    }

    public void add(final CourtEvent courtEvent) {
        this.courtEvents.add(courtEvent);

        courtEvent.setOffenderCourtCase(this);
        courtEvent.setOffenderBooking(getOffenderBooking());
    }
}
