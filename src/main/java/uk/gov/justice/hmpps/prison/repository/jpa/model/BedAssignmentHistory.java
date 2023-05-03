package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.NotFound;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@EqualsAndHashCode(exclude = {"bedAssignmentHistoryPK", "offenderBooking"}, callSuper = false)
@ToString(exclude = {"bedAssignmentHistoryPK", "offenderBooking"})
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BED_ASSIGNMENT_HISTORIES")
@NamedEntityGraph(
    name = "bed-history-with-booking",
    attributeNodes = @NamedAttributeNode(value = "offenderBooking", subgraph = "booking-offender"),
    subgraphs = {
        @NamedSubgraph(
            name = "booking-offender",
            attributeNodes = {
                @NamedAttributeNode("offender")
            }
        )
    }
)
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

    @NotFound(action = IGNORE)
    @ManyToOne
    @JoinColumn(name = "LIVING_UNIT_ID", referencedColumnName = "INTERNAL_LOCATION_ID", insertable = false, updatable = false)
    private AgencyInternalLocation location;

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

    public String movementMadeBy() {
        return super.getCreateUserId();
    }
}
