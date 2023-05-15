package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_PROGRAM_PROFILES")
@ToString(of = {"offenderProgramReferenceId"})
@NamedEntityGraph(
    name = "program-profile-with-course-activity",
    attributeNodes = {@NamedAttributeNode(value = "courseActivity"), @NamedAttributeNode(value = "agencyLocation")}
)
public class OffenderProgramProfile extends AuditableEntity {

    @Id
    @Column(name = "OFF_PRGREF_ID")
    @SequenceGenerator(name = "OFF_PRGREF_ID", sequenceName = "OFF_PRGREF_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFF_PRGREF_ID")
    private Long offenderProgramReferenceId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CRS_ACTY_ID")
    private CourseActivity courseActivity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AGY_LOC_ID")
    private AgencyLocation agencyLocation;

    @Column(name = "OFFENDER_START_DATE")
    private LocalDate startDate;

    @Column(name = "OFFENDER_END_DATE")
    private LocalDate endDate;

    @Column(name = "OFFENDER_PROGRAM_STATUS")
    private String programStatus;

    private Long programId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + OffenderProgramEndReason.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "OFFENDER_END_REASON", referencedColumnName = "code"))
    })
    private OffenderProgramEndReason endReason;

    @Column(name = "OFFENDER_END_REASON", updatable = false, insertable = false)
    private String endReasonCode;

    @Column(name = "OFFENDER_END_COMMENT_TEXT")
    private String endCommentText;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + WaitlistDecisionCode.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "WAITLIST_DECISION_CODE", referencedColumnName = "code", updatable = false, insertable = false))
    })
    private WaitlistDecisionCode waitlistDecision;

    @Column(name = "WAITLIST_DECISION_CODE")
    private String waitlistDecisionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + RejectReasonCode.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "REJECT_REASON_CODE", referencedColumnName = "code"))
    })
    private RejectReasonCode rejectReason;

    @Column(name = "REJECT_REASON_CODE", updatable = false, insertable = false)
    private String rejectReasonCode;

    @Column(name = "REJECT_DATE")
    private LocalDate rejectDate;

    public boolean isCurrentActivity() {
        final var currentDate = LocalDate.now();
        final var isCurrentProgramProfile = programStatus != null && programStatus.equals("ALLOC") &&
            startAndEndDatesSpanDay(startDate, endDate, currentDate);
        final var isCurrentCourseActivity = courseActivity != null &&
            startAndEndDatesSpanDay(courseActivity.getScheduleStartDate(), courseActivity.getScheduleEndDate(), currentDate);
        return isCurrentProgramProfile && isCurrentCourseActivity;
    }

    private boolean startAndEndDatesSpanDay(final LocalDate startDate, final LocalDate endDate, final LocalDate dateToCheck) {
        return isDateBefore(startDate, dateToCheck.plusDays(1))
            && isDateAfter(endDate, dateToCheck);
    }

    private boolean isDateBefore(final LocalDate date, final LocalDate comparedDate) {
        return date != null && date.isBefore(comparedDate);
    }

    private boolean isDateAfter(final LocalDate date, final LocalDate comparedDate) {
        return date == null || date.isAfter(comparedDate);
    }
}
