package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "AGENCY_LOCATIONS")
public class AgencyLocation extends AuditableEntity {

    public static final String IN = "IN";
    public static final String OUT = "OUT";
    public static final String TRN = "TRN";

    @Id
    @Column(name = "AGY_LOC_ID")
    private String id;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "AGENCY_LOCATION_TYPE")
    private String type;
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
}
