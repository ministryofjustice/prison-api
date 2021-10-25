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
@Table(name = "VISITOR_RESTRICTIONS")
public class GlobalVisitorRestriction extends ExtendedAuditableEntity {


    @Id
    @Column(name = "VISITOR_RESTRICTION_ID", nullable = false)
    private Long id;

    @Column(name = "PERSON_ID", nullable = false)
    private Long personId;

    @Column(name = "COMMENT_TXT")
    private String commentText;

    @Column(name = "EFFECTIVE_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + VISIT_RESTRICTION_TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "VISIT_RESTRICTION_TYPE", referencedColumnName = "code", nullable = false))
    })
    private VisitRestrictionType visitRestrictionType;


    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumn(name = "ENTERED_STAFF_ID")
    @Exclude
    private Staff enteredStaffId;  // no referential integrity



    public boolean isActive(){
        return startDate.isBefore(LocalDate.now().plusDays(1)) && (expiryDate == null || expiryDate.isAfter(LocalDate.now().minusDays(1)));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final GlobalVisitorRestriction restiction = (GlobalVisitorRestriction) o;

        return Objects.equals(getId(), restiction.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
