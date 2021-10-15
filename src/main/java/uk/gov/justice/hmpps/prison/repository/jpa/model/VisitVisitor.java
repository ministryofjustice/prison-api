package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Objects;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventOutcome.EVENT_OUTCOME;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus.EVENT_STS;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.VisitOutcomeReason.VISIT_OUTCOME_REASON;

@AllArgsConstructor
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "OFFENDER_VISIT_VISITORS")
public class VisitVisitor extends ExtendedAuditableEntity {

    @SequenceGenerator(name = "OFFENDER_VISIT_VISITOR_ID", sequenceName = "OFFENDER_VISIT_VISITOR_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_VISIT_VISITOR_ID")
    @Id
    @Column(name = "OFFENDER_VISIT_VISITOR_ID", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "OFFENDER_VISIT_ID", nullable = false)
    private Long visitId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private Person person;

    @Column
    private String commentText;

    @Column(name = "ASSISTED_VISIT_FLAG", nullable = false)
    @Type(type = "yes_no")
    private boolean assistedVisit;

    @Column(name = "GROUP_LEADER_FLAG", nullable = false)
    @Type(type = "yes_no")
    private boolean groupLeader;


    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EVENT_STS + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "EVENT_STATUS", referencedColumnName = "code"))
    })
    private EventStatus eventStatus;

    @Column
    private Long eventId;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + VISIT_OUTCOME_REASON + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "OUTCOME_REASON_CODE", referencedColumnName = "code", nullable = false))
    })
    private VisitOutcomeReason outcomeReason;

    /* DB constraint exists: EVENT_OUTCOME IN ('ATT', 'ABS', 'CANC') */
    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EVENT_OUTCOME + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "EVENT_OUTCOME", referencedColumnName = "code", nullable = false))
    })
    private EventOutcome eventOutcome;


    @Override
    public boolean equals(final Object o) {
     if (this == o) return true;
     if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
     final VisitVisitor offender = (VisitVisitor) o;

     return Objects.equals(getId(), offender.getId());
    }

    @Override
    public int hashCode() {
  return Objects.hashCode(getId());
 }
}
