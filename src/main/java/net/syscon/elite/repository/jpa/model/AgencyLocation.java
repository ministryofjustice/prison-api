package net.syscon.elite.repository.jpa.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
