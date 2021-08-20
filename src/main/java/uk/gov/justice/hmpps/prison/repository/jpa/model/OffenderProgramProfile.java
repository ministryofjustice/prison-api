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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID")
    private AgencyLocation agencyLocation;

    @Column(name = "OFFENDER_START_DATE")
    private LocalDate startDate;

    @Column(name = "OFFENDER_END_DATE")
    private LocalDate endDate;

    @Column(name = "OFFENDER_PROGRAM_STATUS")
    private String programStatus;

    public boolean isCurrentWorkActivity() {
        final var currentDate = LocalDate.now();
        final var isCurrentProgramProfile = startDate != null && startDate.isBefore(currentDate.plusDays(1))
            && (endDate == null || endDate.isAfter(currentDate));
        final var isValidCurrentActivity = courseActivity != null &&
            courseActivity.getScheduleStartDate() != null && courseActivity.getScheduleStartDate().isBefore(currentDate.plusDays(1))
            && (courseActivity.getScheduleEndDate() == null || courseActivity.getScheduleEndDate().isAfter(currentDate));
        return isCurrentProgramProfile && isWorkActivity() && isValidCurrentActivity;
    }

    public boolean isWorkActivity() {
        return courseActivity.getCode() != null && !courseActivity.getCode().startsWith("EDU");
    }
}
