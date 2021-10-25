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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
@Table(name = "OFFENDER_PERSON_RESTRICTS")
public class VisitorRestriction extends ExtendedAuditableEntity {


    @Id
    @Column(name = "OFFENDER_PERSON_RESTRICT_ID", nullable = false)
    private Long id;

    @Column(name = "OFFENDER_CONTACT_PERSON_ID", nullable = false)
    private Long offenderContactPersonId;

    private String commentText;

    @Column(name = "RESTRICTION_EFFECTIVE_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "RESTRICTION_EXPIRY_DATE")
    private LocalDate expiryDate;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + VISIT_RESTRICTION_TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "RESTRICTION_TYPE", referencedColumnName = "code", nullable = false))
    })
    private VisitRestrictionType visitRestrictionType;


    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumn(name = "ENTERED_STAFF_ID")
    @Exclude
    private Staff enteredStaffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumn(name = "AUTHORIZED_STAFF_ID")
    @Exclude
    private Staff authorisedStaffId;

    public boolean isActive(){
        return startDate.isBefore(LocalDate.now().plusDays(1)) && (expiryDate == null || expiryDate.isAfter(LocalDate.now().minusDays(1)));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final VisitorRestriction restiction = (VisitorRestriction) o;

        return Objects.equals(getId(), restiction.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
