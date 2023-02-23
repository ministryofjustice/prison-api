package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@Getter
@ToString
@AllArgsConstructor
@Builder
@Entity
@Setter
@RequiredArgsConstructor
@Table(name = "AGENCY_VISIT_SLOTS")
public class AgencyVisitSlot {
    @Id
    @Column(name = "AGENCY_VISIT_SLOT_ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    @Exclude
    private AgencyLocation location;

    @Column(nullable = false)
    private String weekDay;

    @Column(name = "TIME_SLOT_SEQ", nullable = false)
    private Integer timeSlotSequence;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERNAL_LOCATION_ID", nullable = false)
    @Exclude
    private AgencyInternalLocation agencyInternalLocation;

    @Column
    private Integer maxGroups;

    @Column
    private Integer maxAdults;


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final AgencyVisitSlot that = (AgencyVisitSlot) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
