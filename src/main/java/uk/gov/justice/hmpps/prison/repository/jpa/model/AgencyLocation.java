package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "AGENCY_LOCATIONS")
public class AgencyLocation extends AuditableEntity {
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
}
