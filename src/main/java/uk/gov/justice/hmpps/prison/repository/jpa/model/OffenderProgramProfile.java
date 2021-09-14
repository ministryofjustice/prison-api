package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper=false)
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
