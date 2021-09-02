package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus.CASE_STS;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType.LEG_CASE_TYP;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "OFFENDER_CASES")
@ToString(exclude = "offenderBooking")
public class OffenderCourtCase extends AuditableEntity {

    private static final String ACTIVE = "active";

    @Id
    @Column(name = "CASE_ID", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "CASE_SEQ", nullable = false)
    private Long caseSeq;

    private LocalDate beginDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMBINED_CASE_ID")
    private OffenderCourtCase combinedCase;

    @OneToMany(mappedBy = "offenderCourtCase")
    @Default
    private final List<CourtEvent> courtEvents = new ArrayList<>();

    @OneToMany(mappedBy = "offenderCourtCase")
    private final List<OffenderCharge> charges = new ArrayList<>();

    public Optional<LegalCaseType> getLegalCaseType() {
        return Optional.ofNullable(legalCaseType);
    }

    public Optional<CaseStatus> getCaseStatus() {
        return Optional.ofNullable(caseStatus);
    }

    public void add(final OffenderCharge charge) {
        charges.add(charge);
    }

    public boolean isActive() {
        return caseStatus != null && caseStatus.isActive();
    }

    public Collection<OffenderCharge> getCharges(final Predicate<OffenderCharge> filter) {
        return charges.stream().filter(filter).collect(Collectors.toList());
    }
}
