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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@EqualsAndHashCode(callSuper=false, exclude = "parties")
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AGENCY_INCIDENTS")
@ToString(of = {"agencyIncidentId"})
public class Adjudication extends AuditableEntity {

    public static final String INCIDENT_ROLE_OFFENDER = "S";
    public static final String INCIDENT_ROLE_OTHER = "OTH";
    public static final String INCIDENT_ROLE_VICTIM = "V";
    public static final String INCIDENT_STATUS_ACTIVE = "ACTIVE";
    public static final String LOCK_FLAG_UNLOCKED = "N";

    @Id
    @Column(name = "AGENCY_INCIDENT_ID")
    @SequenceGenerator(name = "AGENCY_INCIDENT_ID", sequenceName = "AGENCY_INCIDENT_ID", allocationSize = 1)
    @GeneratedValue(generator = "AGENCY_INCIDENT_ID")
    private Long agencyIncidentId;

    @OneToMany(mappedBy = "id.adjudication", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Default
    private List<AdjudicationParty> parties = new ArrayList<>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORTED_STAFF_ID", nullable = false)
    private Staff staffReporter;

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

    public Optional<AdjudicationParty> getOffenderParty() {
        return parties.stream().filter(p -> INCIDENT_ROLE_OFFENDER.equals(p.getIncidentRole())).findFirst();
    }

    public String getCreatedByUserId() {
        return this.getCreateUserId();
    }

    public List<AdjudicationParty> getVictimsStaffParties(){
        return parties.stream()
            .filter(p -> INCIDENT_ROLE_VICTIM.equals(p.getIncidentRole()))
            .filter(p -> p.getStaff() != null)
            .toList();
    }

    public List<Staff> getVictimsStaff() {
        return getVictimsStaffParties().stream()
            .map(p -> p.getStaff())
            .toList();
    }

    public List<AdjudicationParty> getVictimsOffenderParties() {
        return parties.stream()
            .filter(p -> INCIDENT_ROLE_VICTIM.equals(p.getIncidentRole()))
            .filter(p -> p.getOffenderBooking() != null).toList();
    }

    public List<OffenderBooking> getVictimsOffenderBookings() {
        return getVictimsOffenderParties().stream()
            .map(AdjudicationParty::getOffenderBooking)
            .toList();

    }

    public List<AdjudicationParty> getConnectedOffenderParties() {
        return parties.stream()
            .filter(p -> INCIDENT_ROLE_OFFENDER.equals(p.getIncidentRole()))
            .filter(p -> !p.getId().getPartySeq().equals(1L))
            .filter(p -> p.getOffenderBooking() != null).toList();
    }

    public Optional<AdjudicationParty> getConnectedOffenderPartyWithBookingId(Long bookingId) {
        return getConnectedOffenderParties().stream()
            .filter(p -> p.getOffenderBooking() != null)
            .filter(p -> bookingId.equals(p.getOffenderBooking().getBookingId()))
            .findFirst();
    }

    public List<OffenderBooking> getConnectedOffenderBookings() {
        return getConnectedOffenderParties().stream()
            .map(AdjudicationParty::getOffenderBooking)
            .toList();
    }

    public Long getMaxSequence(){
        return parties.stream().map(p -> p.getId().getPartySeq()).max(Comparator.comparing(Long::valueOf)).orElse(1L);
    }
}
