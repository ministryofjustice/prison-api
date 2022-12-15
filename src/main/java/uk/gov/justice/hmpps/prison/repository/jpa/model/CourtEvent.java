package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus.EVENT_STS;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.REASON;

@Getter
@Entity
@EntityListeners({
    CourtEvent.OnCreate.class,
    CourtEvent.OnDelete.class
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Table(name = "COURT_EVENTS")
@ToString(exclude = {"offenderBooking", "offenderCourtCase"})
@Slf4j
@With
public class CourtEvent extends AuditableEntity {

    @SequenceGenerator(name = "EVENT_ID", sequenceName = "EVENT_ID", allocationSize = 1)
    @GeneratedValue(generator = "EVENT_ID")
    @Id
    @Column(name = "EVENT_ID", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CASE_ID")
    private OffenderCourtCase offenderCourtCase;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "EVENT_DATE", nullable = false)
    private LocalDate eventDate;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + REASON + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "COURT_EVENT_TYPE", referencedColumnName = "code"))
    })
    private MovementReason courtEventType;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EVENT_STS + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "EVENT_STATUS", referencedColumnName = "code"))
    })
    private EventStatus eventStatus;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "OUTCOME_REASON_CODE", nullable = true)
    private OffenceResult outcomeReasonCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
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

    @Column(name = "HOLD_FLAG", length = 1)
    private String holdFlag;

    @Column(name = "PARENT_EVENT_ID")
    private Long parentCourtEventId;

    @OneToMany(mappedBy = "eventAndCharge.courtEvent", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    private final List<CourtEventCharge> charges = new ArrayList<>();

    public Optional<OffenderCourtCase> getOffenderCourtCase() {
        return Optional.ofNullable(offenderCourtCase);
    }

    public LocalDateTime getEventDateTime() {
        return eventDate.atTime(startTime.toLocalTime());
    }

    public void setEventDateTime(final LocalDateTime dateTime) {
        this.eventDate = dateTime.toLocalDate();
        this.startTime = dateTime;
    }

    public void setOutcomeReasonCode(OffenceResult offenceResult) {
        this.outcomeReasonCode = offenceResult;
    }
    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    private void add(final CourtEventCharge charge) {
        charges.add(charge);
    }

    static class OnCreate {
        /**
         * On court event creation on NOMIS existing active charges are applied to the new court event if associated with a court case.
         */
        @PrePersist
        void applyActiveCourtCaseChargesFor(final CourtEvent event) {
            event.getOffenderCourtCase().ifPresent(courtCase -> {
                    courtCase.getCharges(OffenderCharge::isActive).forEach(charge ->
                        event.add(CourtEventCharge.builder()
                            .offenderCharge(charge)
                            .courtEvent(event)
                            .build())
                    );

                    if (!event.getCharges().isEmpty()) {
                        log.debug("Carried over '{}' active charge(s) for court case '{}' to court event '{}'", event.getCharges().size(), courtCase.getId(), event.getId());
                    }
                }
            );
        }
    }

    @Component
    static class OnDelete {

        private Clock clock;

        @Autowired
        void setClock(final Clock clock) {
            this.clock = clock;
        }

        @PreRemove
        void checkIsScheduledFutureEventPriorToRemovalOf(final CourtEvent event) {
            Objects.requireNonNull(clock, "Clock not set.");

            checkState(event.getEventDateTime().isAfter(LocalDateTime.now(clock)), "Court hearing '%s' cannot be deleted as its start date/time is in the past.", event.getId());
            checkState(event.getEventStatus().isScheduled(), "Court hearing '%s' must be in a scheduled state to delete.", event.getId());
        }
    }
}
