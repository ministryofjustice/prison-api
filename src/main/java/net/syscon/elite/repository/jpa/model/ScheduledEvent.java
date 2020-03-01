package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.syscon.elite.repository.jpa.model.ScheduledEventType.EVENT_TYPE;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_IND_SCHEDULES")
public class ScheduledEvent {
    @Id
    @Column(name = "EVENT_ID", nullable = false)
    private Long eventId;

    @OneToOne
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "EVENT_DATE", nullable = false)
    private LocalDate eventDate;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME", nullable = false)
    private LocalDateTime endTime;

    @OneToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EVENT_TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "EVENT_SUB_TYPE", referencedColumnName = "code"))
    })
    private ScheduledEventType scheduledEventType;

    @OneToOne
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    private AgencyLocation agencyLocation;

    @OneToOne
    @NotFound(action = IGNORE)
    @JoinColumn(name = "TO_INTERNAL_LOCATION_ID", nullable = false)
    private AgencyInternalLocation agencyInternalLocation;

    @Column(name = "AUDIT_USER_ID", nullable = false)
    private String auditUserId;
}
