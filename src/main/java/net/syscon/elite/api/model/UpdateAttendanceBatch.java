package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@ApiModel(description = "Attendance details.  This is used to update the attendance details of multiple bookings")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class UpdateAttendanceBatch {

    @ApiModelProperty(required = true, position = 0, value = "Attendance outcome, possible values are the codes in the 'PS_PA_OC' reference domain.", example = "ATT", allowableValues = "ABS,ACCAB,ATT,CANC,NREQ,SUS,UNACAB,REST")
    @Length(max = 12)
    @NotBlank
    private String eventOutcome;

    @ApiModelProperty(value = "Possible values are the codes in the 'PERFORMANCE' reference domain, mandatory for eventOutcome 'ATT'.", position = 1, example = "ACCEPT", allowableValues = "ACCEPT,GOOD,POOR,STANDARD,UNACCEPT")
    @Length(max = 12)
    private String performance;

    @ApiModelProperty(value = "Free text comment, maximum length 240 characters.", position = 2, example = "Turned up very late")
    @Length(max = 240)
    private String outcomeComment;

    @ApiModelProperty(required = true, value = "Set of booking ids")
    private Set<Long> bookingIds;

    @ApiModelProperty(required = true, value = "Set of activity ids")
    private Set<Long> activityIds;

    @ApiModelProperty(required = true, value = "set of booking and activity ids")
    private Set<BookingActivity> bookingActivities;
}

