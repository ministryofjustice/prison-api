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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Where;
import org.hibernate.type.YesNoConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType.AGY_LOC_TYPE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType.JURISDICTION;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AGENCY_LOCATIONS")
@ToString(of = {"id", "description"})
@With
@NamedEntityGraph(
    name = "agency-location-with-contact-details",
    attributeNodes =  { @NamedAttributeNode(value = "addresses", subgraph = "address-phone"), @NamedAttributeNode(value = "phones") },
    subgraphs = {
    @NamedSubgraph(
        name = "address-phone",
        attributeNodes = {
            @NamedAttributeNode("phones"), @NamedAttributeNode("addressUsages")
        }
    )
}
)
public class AgencyLocation extends AuditableEntity {

    public static final String IN = "IN";
    public static final String OUT = "OUT";
    public static final String TRN = "TRN";

    @Id
    @Column(name = "AGY_LOC_ID")
    private String id;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AGY_LOC_TYPE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "AGENCY_LOCATION_TYPE", referencedColumnName = "code", nullable = false))
    })
    private AgencyLocationType type;

    @Column(name = "ACTIVE_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean active;

    @Column(name = "LONG_DESCRIPTION")
    private String longDescription;

    @OneToMany(mappedBy = "agencyLocId", cascade = CascadeType.ALL)
    @Default
    private List<AgencyLocationEstablishment> establishmentTypes = new ArrayList<>();

    @OneToMany(mappedBy = "agencyLocation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Default
    private List<CaseloadAgencyLocation> caseloadAgencyLocations = new ArrayList<>();

    @Column(name = "DEACTIVATION_DATE")
    private LocalDate deactivationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + JURISDICTION + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "JURISDICTION_CODE", referencedColumnName = "code", nullable = false))
    })
    private CourtType courtType;

    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Where(clause = "OWNER_CLASS = '"+AgencyAddress.ADDR_TYPE+"'")
    @Default
    private Set<AgencyAddress> addresses = new HashSet<>();

    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Where(clause = "OWNER_CLASS = '"+AgencyPhone.PHONE_TYPE+"'")
    @Default
    private Set<AgencyPhone> phones = new HashSet<>();

    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Where(clause = "OWNER_CLASS = '"+AgencyInternetAddress.TYPE+"'")
    @Default
    private List<AgencyInternetAddress> internetAddresses = new ArrayList<>();

    public void removeAddress(final AgencyAddress address) {
        addresses.remove(address);
    }

    public AgencyAddress addAddress(final AgencyAddress address) {
        address.setAgency(this);
        addresses.add(address);
        return address;
    }

    public boolean isPrison() {
        return getType().isPrison() && !Arrays.asList(IN, OUT, TRN).contains(getId());
    }

    public boolean isCourt() {
        return getType().isCourt();
    }

    public boolean isHospital() {
        return getType().isHospital();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final AgencyLocation that = (AgencyLocation) o;

        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
