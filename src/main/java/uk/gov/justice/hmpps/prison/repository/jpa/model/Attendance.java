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

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_COURSE_ATTENDANCES")
@ToString(of = {"eventId", "offenderBooking"})
public class Attendance extends AuditableEntity {

    @Id
    @Column(name = "EVENT_ID")
    private Long eventId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CRS_ACTY_ID")
    private CourseActivity courseActivity;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROGRAM_ID")
    private ProgramService programService;

    @Column(name = "EVENT_DATE")
    private LocalDate eventDate;

    @Column(name = "EVENT_OUTCOME")
    private String eventOutcome;
}
