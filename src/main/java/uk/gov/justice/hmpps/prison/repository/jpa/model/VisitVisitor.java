package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
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
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
@NamedEntityGraph(
    name = "visitor-with-person",
    attributeNodes = {
        @NamedAttributeNode(value = "person"),
    }
)
public class VisitVisitor extends AuditableEntity {

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
    @Convert(converter = YesNoConverter.class)
    private boolean assistedVisit;

    @Column(name = "GROUP_LEADER_FLAG", nullable = false)
    @Convert(converter = YesNoConverter.class)
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
            @JoinColumnOrFormula(column = @JoinColumn(name = "OUTCOME_REASON_CODE", referencedColumnName = "code"))
    })
    private VisitOutcomeReason outcomeReason;

    /* DB constraint exists: EVENT_OUTCOME IN ('ATT', 'ABS', 'CANC') */
    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EVENT_OUTCOME + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "EVENT_OUTCOME", referencedColumnName = "code"))
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
