package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Schema(description = "Bed assignment history entry")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class BedAssignment {
    @Schema(description = "Unique, numeric booking id. Used as a primary key when combined with the bed assignment sequence", example = "1234134")
    private Long bookingId;

    @Schema(description = "Identifier of living unit (e.g. cell) that offender is assigned to.", example = "123123")
    private Long livingUnitId;

    @Schema(description = "Date the offender was assigned to a living unit.", example = "2020-10-12")
    private LocalDate assignmentDate;

    @Schema(description = "Date and time the offender was moved to a living unit.", example = "2020-10-12T08:00")
    private LocalDateTime assignmentDateTime;

    @Schema(description = "Assignment reason code", example = "ADM")
    private String assignmentReason;

    @Schema(description = "Date an offender was moved out of the living unit", example = "2020-11-12")
    private LocalDate assignmentEndDate;

    @Schema(description = "Date and time an offender was moved out of the living unit", example = "2020-11-12:T15:00")
    private LocalDateTime assignmentEndDateTime;

    @Schema(description = "Agency of living unit", example = "MDI")
    private String agencyId;

    @Schema(description = "Description of living unit (e.g. cell) ", example = "MDI-1-1-2")
    private String description;

    @Schema(description = "Bed assignment sequence. Used as a primary key when combined with the booking id", example = "2")
    private Integer bedAssignmentHistorySequence;

    @Schema(description = "the staff member responsible for the movement of a prisoner", example = "KQJ74F")
    private String movementMadeBy;

    @Schema(description = "Offender number", example = "A1234AA")
    private String offenderNo;
}
