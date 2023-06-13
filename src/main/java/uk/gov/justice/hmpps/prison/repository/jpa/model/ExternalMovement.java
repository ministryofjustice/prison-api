package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.type.YesNoConverter;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
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
@NamedEntityGraph(
    name = "movement-with-detail-and-offender",
    attributeNodes = {
        @NamedAttributeNode(value = "offenderBooking", subgraph = "booking-offender"),
        @NamedAttributeNode(value = "fromAgency"),
        @NamedAttributeNode(value = "fromCity"),
        @NamedAttributeNode(value = "toAgency"),
        @NamedAttributeNode(value = "toCity"),
        @NamedAttributeNode(value = "movementReason"),
        @NamedAttributeNode(value = "movementType"),
    },
    subgraphs = {
        @NamedSubgraph(
            name = "booking-offender",
            attributeNodes = {
                @NamedAttributeNode("offender"),
                @NamedAttributeNode("assignedLivingUnit"),
            }
        )
    }
)
public class ExternalMovement extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private OffenderBooking offenderBooking;
        private Long movementSequence;
    }

    @Id
    @ManyToOne(optional = false)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FROM_AGY_LOC_ID")
    private AgencyLocation fromAgency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TO_AGY_LOC_ID")
    private AgencyLocation toAgency;

    @Column(name = "ACTIVE_FLAG")
    @Convert(converter = YesNoConverter.class)
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
