package uk.gov.justice.hmpps.prison.repository.jpa.model;

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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
public class OffenderProgramProfile extends ExtendedAuditableEntity {

    @Id
    @Column(name = "OFF_PRGREF_ID")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + OffenderProgramEndReason.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "OFFENDER_END_REASON", referencedColumnName = "code"))
    })
    private OffenderProgramEndReason endReason;

    @Column(name = "OFFENDER_END_COMMENT_TEXT")
    private String endCommentText;

    public boolean isCurrentWorkActivity() {
        return isCurrentActivity() && isWorkActivity();
    }

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

    public boolean isWorkActivity() {
        return courseActivity != null && courseActivity.getCode() != null && !courseActivity.getCode().startsWith("EDU");
    }
}
