package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@AllArgsConstructor
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@ToString
@Table(name = "OFFENDER_VO_VISITORS")
public class VisitOrderVisitor extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_VO_VISITOR_ID", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "OFFENDER_VISIT_ORDER_ID", nullable = false)
    private VisitOrder visitOrder;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private Person person;

    @Column(name = "GROUP_LEADER_FLAG", nullable = false)
    @Convert(converter = YesNoConverter.class)
    private boolean groupLeader;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final VisitOrderVisitor offender = (VisitOrderVisitor) o;

        return Objects.equals(getId(), offender.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
