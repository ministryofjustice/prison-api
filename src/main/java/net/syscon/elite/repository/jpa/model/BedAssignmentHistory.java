package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BED_ASSIGNMENT_HISTORIES")
public class BedAssignmentHistory extends AuditableEntity {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class BookingAndSequence implements Serializable {
        @ManyToOne(optional = false)
        @JoinColumn(name = "OFFENDER_BOOK_ID")
        private Long offenderBookingId;

        @Column(name = "BED_ASSIGN_SEQ", nullable = false)
        private Integer sequence;
    }

    @EmbeddedId
    private BookingAndSequence bookingAndSequence;

    @Column(name = "LIVING_UNIT_ID")
    private Long livingUnitId;

    @Column(name = "ASSIGNMENT_DATE")
    private LocalDate assignmentDate;

    @Column(name = "ASSIGNMENT_TIME")
    private LocalDateTime assignmentDateTime;

    @Column(name = "ASSIGNMENT_REASON")
    private String assignmentReason;

    @Column(name = "ASSIGNMENT_END_DATE")
    private LocalDate assignmentEndDate;

    @Column(name = "ASSIGNMENT_END_TIME")
    private LocalDateTime assignmentEndDateTime;

}
