package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_COURSE_ATTENDANCES")
public class OffenderCourseAttendance extends AuditableEntity {

    @Id
    @Column(name = "EVENT_ID")
    private long eventId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "OFFENDER_BOOK_ID", referencedColumnName = "OFFENDER_BOOK_ID", insertable = false, updatable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "EVENT_DATE")
    private LocalDate eventDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CRS_ACTY_ID", referencedColumnName = "CRS_ACTY_ID", insertable = false, updatable = false)
    private CourseActivity courseActivity;
}
