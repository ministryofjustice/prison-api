package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.Where;
import org.hibernate.type.YesNoConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Getter
@ToString
@AllArgsConstructor
@Builder
@Entity
@Setter
@RequiredArgsConstructor
@Table(name = "AGENCY_INTERNAL_LOCATIONS")
@NamedEntityGraph(
    name = "agency-internal-location-with-profiles",
    attributeNodes = @NamedAttributeNode(value = "profiles", subgraph = "housing-units"),
    subgraphs = {
        @NamedSubgraph(
            name = "housing-units",
            attributeNodes = {
                @NamedAttributeNode("housingAttributeReferenceCode")
            }
        )
    }
)
public class AgencyInternalLocation {
    @Id
    @Column(name = "INTERNAL_LOCATION_ID")
    private Long locationId;

    @Column(name = "ACTIVE_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean active;

    @Column(name = "CERTIFIED_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean certifiedFlag;

    @Column(name = "INTERNAL_LOCATION_TYPE")
    private String locationType;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + LivingUnitReferenceCode.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "INTERNAL_LOCATION_TYPE", referencedColumnName = "code", insertable = false, updatable = false))
    })
    private LivingUnitReferenceCode livingUnit;

    @Column(name = "AGY_LOC_ID")
    private String agencyId;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_INTERNAL_LOCATION_ID")
    @Exclude
    private AgencyInternalLocation parentLocation;

    @OneToMany(mappedBy = "parentLocation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Exclude
    @Default
    private List<AgencyInternalLocation> childLocations = new ArrayList<>();

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

    @Column(name = "UNIT_TYPE")
    private String type;

    @OneToMany(fetch = FetchType.LAZY)
    @Where(clause = "INT_LOC_PROFILE_TYPE = 'HOU_UNIT_ATT' AND INT_LOC_PROFILE_CODE is not NULL")
    @JoinColumn(name = "INTERNAL_LOCATION_ID", referencedColumnName = "INTERNAL_LOCATION_ID")
    private List<AgencyInternalLocationProfile> profiles;

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
