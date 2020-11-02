package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;


@ApiModel(description = "Bed assignment history entry")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class BedAssignment {
    @ApiModelProperty(value = "Unique, numeric booking id.", position = 1, example = "1234134")
    private Long bookingId;

    @ApiModelProperty(value = "Identifier of living unit (e.g. cell) that offender is assigned to.", position = 2, example = "123123")
    private Long livingUnitId;

    @ApiModelProperty(value = "Date the offender was assigned to a living unit.", position = 3, example = "2020-10-12")
    private LocalDate assignmentDate;

    @ApiModelProperty(value = "Date and time the offender was moved to a living unit.", position = 4, example = "2020-10-12T08:00")
    private LocalDateTime assignmentDateTime;

    @ApiModelProperty(value = "Assignment reason code", position = 5, example = "ADM")
    private String assignmentReason;

    @ApiModelProperty(value = "Date an offender was moved out of the living unit", position = 6, example = "2020-11-12")
    private LocalDate assignmentEndDate;

    @ApiModelProperty(value = "Date and time an offender was moved out of the living unit", position = 7, example = "2020-11-12:T15:00")
    private LocalDateTime assignmentEndDateTime;

    @ApiModelProperty(value = "Agency of living unit", position = 8, example = "MDI")
    private String agencyId;

    @ApiModelProperty(value = "Description of living unit (e.g. cell) ", position = 9, example = "MDI-1-1-2")
    private String description;

    @ApiModelProperty(value = "Bed assignment sequence", position = 10, example = "2")
    private Integer badAssignmentHistorySequence;
}
