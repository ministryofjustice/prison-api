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

    @ManyToOne(fetch = FetchType.LAZY)
    // TODO - @NotFound(action = IGNORE)
    @JoinColumn(name = "CRS_ACTY_ID")
    // TODO - @MapsId("bookingId")
    private CourseActivity courseActivity;

    @Column(name = "OFFENDER_START_DATE")
    private LocalDate startDate;

    @Column(name = "OFFENDER_END_DATE")
    private LocalDate endDate;

    @Column(name = "OFFENDER_PROGRAM_STATUS")
    private String programStatus;
}
