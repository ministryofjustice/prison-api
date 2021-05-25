package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Where;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType.AGY_LOC_TYPE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType.JURISDICTION;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "AGENCY_LOCATIONS")
@ToString(of = {"id", "description"})
public class AgencyLocation extends ExtendedAuditableEntity {

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
    @Enumerated(EnumType.STRING)
    private ActiveFlag activeFlag;
    @Column(name = "LONG_DESCRIPTION")
    private String longDescription;
    @OneToMany(mappedBy = "agencyLocId", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AgencyLocationEstablishment> establishmentTypes = new ArrayList<>();

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
    @Builder.Default
    private List<AgencyAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Where(clause = "OWNER_CLASS = '"+AgencyPhone.PHONE_TYPE+"'")
    @Builder.Default
    private List<AgencyPhone> phones = new ArrayList<>();

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
}
