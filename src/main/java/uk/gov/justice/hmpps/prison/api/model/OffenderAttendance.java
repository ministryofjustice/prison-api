package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;


@ApiModel(description = "Information about an Offender's attendance at an activity")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderAttendance {
    @NotNull
    private Long bookingId;

    @NotNull
    @ApiModelProperty(value = "The date of this activity")
    private LocalDate eventDate;

    @ApiModelProperty(value = "Whether the offender attended", allowableValues = "ABS,ACCAB,ATT,CANC,NREQ,SUS,UNACAB,REST")
    private String outcome;

    @ApiModelProperty(value = "The course code")
    private String code;

    @ApiModelProperty(value = "The course description")
    private String description;

    @ApiModelProperty(value = "The current status for the offender on this activity")
    private String activityStatus;

    @ApiModelProperty(value = "Activity name")
    private String activity;

    @ApiModelProperty(value = "Attendance comment")
    private String comment;
}
