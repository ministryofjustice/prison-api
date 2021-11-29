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
@ToString(of = {"eventId"})
public class Attendance extends AuditableEntity {

    @Id
    @Column(name = "EVENT_ID")
    private Long eventId;

    // TODO not sure yet which columns we mignt need
//    @ManyToOne(optional = false, fetch = FetchType.LAZY)
//    @JoinColumn(name = "OFF_PRGREF_ID", nullable = false)
//    private OffenderProgramProfile offenderProgramProfile;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "CRS_ACTY_ID")
//    private CourseActivity courseActivity;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "EVENT_DATE")
    private LocalDate eventDate;

    @Column(name = "EVENT_OUTCOME")
    private String eventOutcome;
}
