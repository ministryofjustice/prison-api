package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Type;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static javax.persistence.EnumType.STRING;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.City.CITY;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.REASON;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.REL;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TYPE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_EXTERNAL_MOVEMENTS")
@IdClass(ExternalMovement.PK.class)
@EqualsAndHashCode(callSuper = false)
public class ExternalMovement extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private OffenderBooking offenderBooking;
        private Long movementSequence;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Id
    @Column(name = "MOVEMENT_SEQ")
    private Long movementSequence;

    @Column(name = "MOVEMENT_DATE")
    private LocalDate movementDate;

    @Column(name = "REPORTING_DATE")
    private LocalDate reportingDate;

    @Column(name = "MOVEMENT_TIME")
    private LocalDateTime movementTime;

    //It will be COURT_EVENTS ID or OFFENDER_IND_SCHEDULES ID
    @Column(name = "EVENT_ID")
    private Long eventId;

    @Column(name = "PARENT_EVENT_ID")
    private Long parentEventId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ARREST_AGENCY_LOC_ID")
    private AgencyLocation arrestAgencyLocation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "FROM_AGY_LOC_ID")
    private AgencyLocation fromAgency;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "TO_AGY_LOC_ID")
    private AgencyLocation toAgency;

    @Column(name = "ACTIVE_FLAG")
    @Type(type="yes_no")
    @Default
    private boolean active = true;

    @Column(name = "ESCORT_TEXT")
    private String escortText;

    @Column(name = "ESCORT_Code")
    private String escortCode;

    @Column(name = "COMMENT_TEXT")
    private String commentText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CITY + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "TO_CITY", referencedColumnName = "code"))
    })
    private City toCity;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CITY + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "FROM_CITY", referencedColumnName = "code"))
    })
    private City fromCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + REASON + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "MOVEMENT_REASON_CODE", referencedColumnName = "code"))
    })
    private MovementReason movementReason;

    @Enumerated(STRING)
    @Column(name = "DIRECTION_CODE")
    private MovementDirection movementDirection;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "MOVEMENT_TYPE", referencedColumnName = "code"))
    })
    private MovementType movementType;

    @Column(name = "TO_ADDRESS_ID")
    private Long toAddressId;

    @Column(name = "FROM_ADDRESS_ID")
    @Nullable
    private Long fromAddressId;

    public String calculateReleaseLocationDescription() {
        return REL.getCode().equals(getMovementType().getCode())
            ? "Outside - released from " + getFromAgency().getDescription()
            : "Outside - " + getMovementType().getDescription();
    }

}
