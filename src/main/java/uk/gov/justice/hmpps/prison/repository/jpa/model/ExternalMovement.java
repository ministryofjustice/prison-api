package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
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
        @Column(name = "OFFENDER_BOOK_ID", updatable = false, insertable = false)
        private Long bookingId;

        @Column(name = "MOVEMENT_SEQ", updatable = false, insertable = false)
        private Long movementSequence;
    }

    @Id
    private Long bookingId;

    @Id
    private Long movementSequence;

    @Column(name = "MOVEMENT_DATE")
    private LocalDate movementDate;

    @Column(name = "REPORTING_DATE")
    private LocalDate reportingDate;

    @Column(name = "MOVEMENT_TIME")
    private LocalDateTime movementTime;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ARREST_AGENCY_LOC_ID")
    private AgencyLocation arrestAgencyLocation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "FROM_AGY_LOC_ID")
    private AgencyLocation fromAgency;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "TO_AGY_LOC_ID")
    private AgencyLocation toAgency;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false, updatable = false, insertable = false)
    private OffenderBooking booking;

    @Enumerated(EnumType.STRING)
    private ActiveFlag activeFlag;

    @Column(name = "ESCORT_TEXT")
    private String escortText;

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


}
