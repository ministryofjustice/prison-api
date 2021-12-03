package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Getter
@ToString
@AllArgsConstructor
@Builder
@Entity
@Setter
@RequiredArgsConstructor
@Table(name = "AGENCY_INTERNAL_LOCATIONS")
@BatchSize(size = 25)
public class AgencyInternalLocation {
    @Id
    @Column(name = "INTERNAL_LOCATION_ID")
    private Long locationId;

    @Column(name = "ACTIVE_FLAG")
    @Type(type="yes_no")
    private boolean active;

    @Column(name = "CERTIFIED_FLAG")
    @Type(type="yes_no")
    private boolean certifiedFlag;

    @Column(name = "INTERNAL_LOCATION_TYPE")
    private String locationType;

    @Column(name = "AGY_LOC_ID")
    private String agencyId;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_INTERNAL_LOCATION_ID")
    @Exclude
    private AgencyInternalLocation parentLocation;

    @Column(name = "NO_OF_OCCUPANT")
    private Integer currentOccupancy;

    @Column(name = "OPERATION_CAPACITY")
    private Integer operationalCapacity;

    @Column(name = "USER_DESC")
    private String userDescription;

    @Column(name = "INTERNAL_LOCATION_CODE")
    private String locationCode;

    @Column(name = "CAPACITY")
    private Integer capacity;

    public boolean isCell() {
        return locationType != null && locationType.equals("CELL");
    }

    public boolean isCellSwap() {
        return !isCertifiedFlag() &&
                isActive() &&
                parentLocation == null &&
                locationCode != null &&
                locationCode.equals("CSWAP");
    }

    private boolean isActiveCell() {
        return isActive() && isCell();
    }

    public boolean hasSpace(final boolean treatZeroOperationalCapacityAsNull) {
        final var capacity = getActualCapacity(treatZeroOperationalCapacityAsNull);
        return capacity != null && currentOccupancy != null && currentOccupancy < capacity;
    }

    public Integer decrementCurrentOccupancy() {
        if (currentOccupancy != null && currentOccupancy > 0) {
            currentOccupancy = currentOccupancy - 1;
        } else {
            currentOccupancy = 0;
        }

        return currentOccupancy;
    }

    public boolean isActiveCellWithSpace(final boolean treatZeroOperationalCapacityAsNull) {
        return isActiveCell() && hasSpace(treatZeroOperationalCapacityAsNull);
    }

    public Integer getActualCapacity(final boolean treatZeroOperationalCapacityAsNull) {
        final var useOperationalCapacity = operationalCapacity != null && !(treatZeroOperationalCapacityAsNull && operationalCapacity == 0);
        return useOperationalCapacity ? operationalCapacity : capacity;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final AgencyInternalLocation that = (AgencyInternalLocation) o;
        return Objects.equals(getLocationId(), that.getLocationId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getLocationId());
    }
}
