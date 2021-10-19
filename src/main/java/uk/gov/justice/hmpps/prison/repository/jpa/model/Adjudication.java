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
import org.hibernate.annotations.NotFound;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AGENCY_INCIDENTS")
@ToString(of = {"agencyIncidentId"})
public class Adjudication extends ExtendedAuditableEntity {

    @Id
    @Column(name = "AGENCY_INCIDENT_ID")
    @SequenceGenerator(name = "AGENCY_INCIDENT_ID", sequenceName = "AGENCY_INCIDENT_ID", allocationSize = 1)
    @GeneratedValue(generator = "AGENCY_INCIDENT_ID")
    private Long agencyIncidentId;

    @OneToMany(mappedBy = "id.adjudication", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Default
    private List<AdjudicationParties> parties = new ArrayList<>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORTED_STAFF_ID", nullable = false)
    private Staff staffReporterId;

    @Column(name = "INCIDENT_DATE", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "INCIDENT_TIME", nullable = false)
    private LocalDateTime incidentTime;

    @Column(name = "REPORT_DATE", nullable = false)
    private LocalDate reportDate;

    @Column(name = "REPORT_TIME", nullable = false)
    private LocalDateTime reportTime;

    @Column(name = "INCIDENT_DETAILS", nullable = false)
    private String incidentDetails;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    private AgencyLocation agencyLocation;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERNAL_LOCATION_ID", nullable = false)
    private AgencyInternalLocation internalLocation;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AdjudicationIncidentType.TYPE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "INCIDENT_TYPE", referencedColumnName = "code"))
    })
    private AdjudicationIncidentType incidentType;

    @Column(name = "INCIDENT_STATUS", nullable = false)
    private String incidentStatus;

    @Column(name = "LOCK_FLAG", nullable = false)
    private String lockFlag;
}
