package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.SearchLevel.SEARCH_LEVEL;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.VisitStatus.VISIT_STATUS;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.VisitType.VISIT_TYPE;

@AllArgsConstructor
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "OFFENDER_VISITS")
public class Visit extends AuditableEntity {

    @SequenceGenerator(name = "OFFENDER_VISIT_ID", sequenceName = "OFFENDER_VISIT_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_VISIT_ID")
    @Id
    @Column(name = "OFFENDER_VISIT_ID", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column
    private String commentText;

    @Column
    private String visitorConcernText;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + VISIT_TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "VISIT_TYPE", referencedColumnName = "code", nullable = false))
    })
    private VisitType visitType;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + VISIT_STATUS + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "VISIT_STATUS", referencedColumnName = "code", nullable = false))
    })
    private VisitStatus visitStatus;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    @Exclude
    private AgencyLocation location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VISIT_INTERNAL_LOCATION_ID")
    @Exclude
    private AgencyInternalLocation agencyInternalLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AGENCY_VISIT_SLOT_ID")
    @Exclude
    private AgencyVisitSlot agencyVisitSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_VISIT_ORDER_ID")
    @Exclude
    private VisitOrder visitOrder;

    /* a list of all visitors including those without visitor orders */
    @OneToMany
    @JoinColumn(name = "OFFENDER_VISIT_ID", referencedColumnName = "OFFENDER_VISIT_ID")
    @Exclude
    @Default
    private List<VisitVisitor> visitors = new ArrayList<>();


    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + SEARCH_LEVEL + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "SEARCH_TYPE", referencedColumnName = "code", nullable = false))
    })
    private SearchLevel searchLevel;


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final Visit offender = (Visit) o;

        return Objects.equals(getId(), offender.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }



    /* fields not used in production for info:

        RAISED_INCIDENT_NUMBER
        private Long raisedIncidentNumber;

        RAISED_INCIDENT_TYPE
        private IncidentType incidentType;

        OUTCOME_REASON_CODE
        private VisitOutcomeReason outcomeReason;

        "CLIENT_UNIQUE_REF" - not used since 2018
        private String clientReference;

        "EVENT_OUTCOME" - not used since 2015
        private VisitOutcome outcome;

        "OVERRIDE_BAN_STAFF_ID" - not used since 2015
        private Long overrideBanStaffId;
     */

}
