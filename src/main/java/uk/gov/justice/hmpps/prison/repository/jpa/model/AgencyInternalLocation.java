package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Data
@Table(name = "AGENCY_INTERNAL_LOCATIONS")
public class AgencyInternalLocation {
    @Id
    @Column(name = "INTERNAL_LOCATION_ID")
    private Long locationId;

    @Column(name = "ACTIVE_FLAG")
    @Enumerated(EnumType.STRING)
    private ActiveFlag activeFlag;

    @Column(name = "CERTIFIED_FLAG")
    @Enumerated(EnumType.STRING)
    private ActiveFlag certifiedFlag;

    @Column(name = "INTERNAL_LOCATION_TYPE")
    private String locationType;

    @Column(name = "AGY_LOC_ID")
    private String agencyId;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_INTERNAL_LOCATION_ID")
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

    public boolean isActive() {
        return activeFlag != null && activeFlag.isActive();
    }

    public boolean isCertified() {
        return certifiedFlag != null && certifiedFlag.isActive();
    }

    public boolean isCell() {
        return locationType != null && locationType.equals("CELL");
    }

    public boolean isCellSwap() {
        return (certifiedFlag == null || !certifiedFlag.isActive()) &&
                (activeFlag == null || activeFlag.isActive()) &&
                parentLocation == null &&
                locationCode != null &&
                locationCode.equals("CSWAP");
    }

    private boolean isActiveCell() {
        return isActive() && isCell();
    }

    private boolean hasSpace() {
        if (operationalCapacity != null && currentOccupancy != null ) {
            return currentOccupancy < operationalCapacity;
        }
        return capacity != null && currentOccupancy != null && currentOccupancy < capacity;
    }

    public Integer incrementCurrentOccupancy() {
        if (currentOccupancy != null) {
            currentOccupancy = currentOccupancy + 1;
        }

        return currentOccupancy;
    }

    public boolean isActiveCellWithSpace() {
        return isActiveCell() && hasSpace();
    }
}
