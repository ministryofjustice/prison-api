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
import javax.persistence.Id;
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

    @Column(name = "INTERNAL_LOCATION_TYPE")
    private String locationType;

    @Column(name = "AGY_LOC_ID")
    private String agencyId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PARENT_INTERNAL_LOCATION_ID")
    private Long parentLocationId;

    @Column(name = "NO_OF_OCCUPANT")
    private Integer currentOccupancy;

    @Column(name = "OPERATION_CAPACITY")
    private Integer operationalCapacity;

    @Column(name = "USER_DESC")
    private String userDescription;

    @Column(name = "INTERNAL_LOCATION_CODE")
    private String locationCode;

    public boolean isActive() {
        return activeFlag.isActive();
    }

    public boolean isCell() {
        return locationType != null && locationType.equals("CELL");
    }
}
