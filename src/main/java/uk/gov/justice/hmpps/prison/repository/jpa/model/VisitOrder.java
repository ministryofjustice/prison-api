package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.VisitOrderType.VISIT_ORDER_TYPE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.VisitStatus.VISIT_STATUS;

@AllArgsConstructor
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "OFFENDER_VISIT_ORDERS")
public class VisitOrder extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_VISIT_ORDER_ID", nullable = false)
    private Long id;

    @Column(name = "VISIT_ORDER_NUMBER", nullable = false)
    private Long visitOrderNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @OneToMany(mappedBy = "visitOrder")
    @Exclude
    @Default
    private List<VisitOrderVisitor> visitors = new ArrayList<>();

    @Column
    private String commentText;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + VISIT_ORDER_TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "VISIT_ORDER_TYPE", referencedColumnName = "code", nullable = false))
    })
    private VisitOrderType visitOrderType;

    /* status content seems to be mainly set to scheduled and the other options don't all make sense */
    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + VISIT_STATUS + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "STATUS", referencedColumnName = "code", nullable = false))
    })
    private VisitStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumn(name = "AUTHORISED_STAFF_ID")
    @Exclude
    private Staff authorisedStaffId;

    @Column
    private LocalDate mailedDate;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final VisitOrder offender = (VisitOrder) o;

        return Objects.equals(getId(), offender.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
