package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_COURSE_ATTENDANCES")
@ToString(of = {"eventId", "offenderBookingId"})
public class Attendance extends ExtendedAuditableEntity {

    @Id
    @Column(name = "EVENT_ID")
    private Long eventId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFF_PRGREF_ID", nullable = false)
    private OffenderProgramProfile offenderProgramProfile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CRS_ACTY_ID")
    private CourseActivity courseActivity;

    // Very slow to get data from this table (5+ times) - even with EAGER
    // Could try @Fetch(FetchMode.JOIN) to force it to use LEFT OUTER JOIN
    // .. but that is probably very slow as well!
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CRS_SCH_ID")
    private CourseSchedule courseSchedule;

    // Prevents expensive join via OffenderProgramProfile table
    @Column(name = "OFFENDER_BOOK_ID")
    private Long offenderBookingId;

    @Column(name = "EVENT_DATE")
    private LocalDate eventDate;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "EVENT_OUTCOME")
    private String eventOutcome;
}
