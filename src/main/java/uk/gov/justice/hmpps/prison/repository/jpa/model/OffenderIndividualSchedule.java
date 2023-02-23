package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.annotations.NotFoundAction.EXCEPTION;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EscortAgencyType.ESCORT_CODE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus.EVENT_STS;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.TransferCancellationReason.TRANSFER_CANCELLATION_REASON;

@Data
@Entity
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "OFFENDER_IND_SCHEDULES")
@ToString(exclude = {"offenderBooking"})
public class OffenderIndividualSchedule extends AuditableEntity {

    public enum EventClass {
        EXT_MOV, INT_MOV, COMM
    }

    @SequenceGenerator(name = "EVENT_ID", sequenceName = "EVENT_ID", allocationSize = 1)
    @GeneratedValue(generator = "EVENT_ID")
    @Id
    @Column(name = "EVENT_ID", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "EVENT_DATE", nullable = false)
    private LocalDate eventDate;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Enumerated(STRING)
    @Column(name = "EVENT_CLASS", nullable = false)
    private EventClass eventClass;

    // Mapped to string, this is an overloaded reference code. Requires a discriminator value and subclass and is not worth the effort.
    @Column(name = "EVENT_TYPE", nullable = false)
    private String eventType;

    // Mapped to string, this is an overloaded reference code. Requires a discriminator value and subclass and is not worth the effort.
    @Column(name = "EVENT_SUB_TYPE", nullable = false)
    private String eventSubType;

    @Column(name = "PARENT_EVENT_ID")
    private Long parentEventId;

    @ManyToOne
    @NotFound(action = EXCEPTION)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EVENT_STS + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "EVENT_STATUS", referencedColumnName = "code"))
    })
    private EventStatus eventStatus;

    // This is nullable on the DB but in the context of how we use this it should (always?) be populated.
    @ManyToOne
    @JoinColumn(name = "AGY_LOC_ID")
    private AgencyLocation fromLocation;

    // This is nullable on the DB but in the context of how we use this it should (always?) be populated.
    @ManyToOne
    @JoinColumn(name = "TO_AGY_LOC_ID")
    private AgencyLocation toLocation;

    @ManyToOne
    @NotFound(action = EXCEPTION)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + ESCORT_CODE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ESCORT_CODE", referencedColumnName = "code"))
    })
    private EscortAgencyType escortAgencyType;

    @Enumerated(STRING)
    @Column(name = "DIRECTION_CODE")
    private MovementDirection movementDirection;

    @ManyToOne
    @NotFound(action = EXCEPTION)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + TRANSFER_CANCELLATION_REASON + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "OUTCOME_REASON_CODE", referencedColumnName = "code"))
    })
    private TransferCancellationReason cancellationReason;

    @Column(name = "TO_ADDRESS_ID")
    private Long toAddressId;

    @Column(name = "TO_ADDRESS_OWNER_CLASS")
    private String toAddressOwnerClass;

    public LocalDateTime getEventDateTime() {
        return eventDate.atTime(startTime.toLocalTime());
    }
}
