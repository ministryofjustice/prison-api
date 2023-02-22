package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.VisitRestrictionType.VISIT_RESTRICTION_TYPE;

@AllArgsConstructor
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "OFFENDER_RESTRICTIONS")
public class OffenderRestriction extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_RESTRICTION_ID", nullable = false)
    private Long id;

    @Column(name = "OFFENDER_BOOK_ID", nullable = false)
    private Long offenderBookingId;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + VISIT_RESTRICTION_TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "RESTRICTION_TYPE", referencedColumnName = "code", nullable = false))
    })
    private VisitRestrictionType visitRestrictionType;

    @Column(name = "RESTRICTION_TYPE", updatable = false, insertable = false)
    private String restrictionType;

    @Column(name = "COMMENT_TEXT")
    private String commentText;

    @Column(name = "EFFECTIVE_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumn(name = "ENTERED_STAFF_ID")
    @Exclude
    private Staff enteredStaffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumn(name = "AUTHORISED_STAFF_ID")
    @Exclude
    private Staff authorisedStaffId;

    public boolean isActive(){
        return startDate.isBefore(LocalDate.now().plusDays(1)) && (expiryDate == null || expiryDate.isAfter(LocalDate.now().minusDays(1)));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final OffenderRestriction restriction = (OffenderRestriction) o;

        return Objects.equals(getId(), restriction.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
