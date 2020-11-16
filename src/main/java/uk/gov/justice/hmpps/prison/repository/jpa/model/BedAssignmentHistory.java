package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
@EqualsAndHashCode(exclude = {"bedAssignmentHistoryPK", "offenderBooking"}, callSuper = false)
@ToString(exclude = {"bedAssignmentHistoryPK", "offenderBooking"})
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BED_ASSIGNMENT_HISTORIES")
public class BedAssignmentHistory extends AuditableEntity {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class BedAssignmentHistoryPK implements Serializable {
        @Column(name = "OFFENDER_BOOK_ID", nullable = false)
        private Long offenderBookingId;

        @Column(name = "BED_ASSIGN_SEQ", nullable = false)
        private Integer sequence;
    }

    @EmbeddedId
    private BedAssignmentHistoryPK bedAssignmentHistoryPK;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_BOOK_ID", insertable = false, updatable = false)
    private OffenderBooking offenderBooking;

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

    public String getCreatedUserId() {
        return super.getCreateUserId();
    }
}
