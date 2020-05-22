package net.syscon.elite.repository.jpa.model;

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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.syscon.elite.repository.jpa.model.EventStatus.EVENT_STS;
import static net.syscon.elite.repository.jpa.model.EventType.EVENT_TYPE;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "COURT_EVENTS")
@ToString(exclude = {"offenderBooking", "offenderCourtCase"})
public class CourtEvent extends AuditableEntity {

    @SequenceGenerator(name = "EVENT_ID", sequenceName = "EVENT_ID", allocationSize = 1)
    @GeneratedValue(generator = "EVENT_ID")
    @Id
    @Column(name = "EVENT_ID", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CASE_ID")
    private OffenderCourtCase offenderCourtCase;

    @ManyToOne(optional = false)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "EVENT_DATE", nullable = false)
    private LocalDate eventDate;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EVENT_TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "COURT_EVENT_TYPE", referencedColumnName = "code"))
    })
    private EventType courtEventType;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EVENT_STS + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "EVENT_STATUS", referencedColumnName = "code"))
    })
    private EventStatus eventStatus;

    @ManyToOne(optional = false)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    private AgencyLocation courtLocation;

    @Column(name = "COMMENT_TEXT", length = 240)
    private String commentText;

    @Default
    @Column(name = "NEXT_EVENT_REQUEST_FLAG", length = 1)
    private String nextEventRequestFlag = "N";

    @Default
    @Column(name = "ORDER_REQUESTED_FLAG", length = 1)
    private String orderRequestedFlag = "N";

    @Column(name = "DIRECTION_CODE", length = 12)
    private String directionCode;

    @OneToMany(mappedBy = "courtEvent", cascade = CascadeType.REMOVE)
    private final List<CourtEventCharge> charges = new ArrayList<>();

    public Optional<OffenderCourtCase> getOffenderCourtCase() {
        return Optional.ofNullable(offenderCourtCase);
    }

    public LocalDateTime getEventDateTime() {
        return eventDate.atTime(startTime.toLocalTime());
    }
}
