package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.*;

import java.util.List;

import static net.syscon.elite.repository.jpa.model.ReferenceCode.*;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_VISIT_VISITORS")
public class OffenderVisitVisitor {
    @Id
    @Column(name = "OFFENDER_VISIT_VISITOR_ID", nullable = false)
    private Long visitVisitorId;

    @Column(name = "OFFENDER_VISIT_ID", nullable = false)
    private Long visitId;

    @Column(name = "GROUP_LEADER_FLAG", nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupLeaderFlag groupLeaderFlag;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EVENT_STS + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "EVENT_STS", referencedColumnName = "code"))
    })
    private EventStatus eventStatus;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + OUTCOMES + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "OUTCOMES", referencedColumnName = "code"))
    })
    private EventOutcome eventOutcome;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + MOVE_CANC_RS + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "MOVE_CANC_RS", referencedColumnName = "code"))
    })
    private OutcomeReason outcomeReason;

    @OneToOne
    @JoinColumn(name = "PERSON_ID")
    private Person person;

}
