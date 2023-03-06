package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import lombok.With;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus.CASE_STS;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType.LEG_CASE_TYP;

@Getter
@Setter
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "OFFENDER_CASES")
@ToString
@With
public class OffenderCourtCase extends AuditableEntity {

    private static final String ACTIVE = "active";

    @Id
    @Column(name = "CASE_ID")
    @SequenceGenerator(name = "CASE_ID", sequenceName = "CASE_ID", allocationSize = 1)
    @GeneratedValue(generator = "CASE_ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    @Exclude
    private OffenderBooking offenderBooking;

    @Column(name = "CASE_SEQ", nullable = false)
    private Integer caseSeq;

    private LocalDate beginDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    @Exclude
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
    @Exclude
    private OffenderCourtCase combinedCase;

    @OneToMany(mappedBy = "offenderCourtCase")
    @Default
    @Exclude
    private final List<CourtEvent> courtEvents = new ArrayList<>();

    @OneToMany(mappedBy = "offenderCourtCase")
    @Exclude
    private final List<OffenderCharge> charges = new ArrayList<>();

    @OneToMany(mappedBy = "courtCase")
    @Default
    @Exclude
    private final List<OffenderSentence> sentences = new ArrayList<>();

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
        return caseStatus != null && caseStatus.isActiveStatus();
    }

    public Collection<OffenderCharge> getCharges(final Predicate<OffenderCharge> filter) {
        return charges.stream().filter(filter).toList();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final OffenderCourtCase that = (OffenderCourtCase) o;
        return id != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
