package net.syscon.elite.repository.jpa.model;

import lombok.*;

import javax.persistence.*;

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
