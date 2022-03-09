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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Attendance details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Attendance details.  This is used to update the attendance details of an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class UpdateAttendance {

    @ApiModelProperty(required = true, position = 0, value = "Attendance outcome, possible values are the codes in the 'PS_PA_OC' reference domain.", example = "ATT", allowableValues = "ABS,ACCAB,ATT,CANC,NREQ,SUS,UNACAB,REST")
    @Size(max = 12)
    @NotBlank
    private String eventOutcome;

    @ApiModelProperty(value = "Possible values are the codes in the 'PERFORMANCE' reference domain, mandatory for eventOutcome 'ATT'.", position = 1, example = "ACCEPT", allowableValues = "ACCEPT,GOOD,POOR,STANDARD,UNACCEPT")
    @Size(max = 12)
    private String performance;

    @ApiModelProperty(value = "Free text comment, maximum length 240 characters.", position = 2, example = "Turned up very late")
    @Size(max = 240)
    private String outcomeComment;

}
