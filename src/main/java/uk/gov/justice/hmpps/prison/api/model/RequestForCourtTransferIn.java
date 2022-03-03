package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@ApiModel(description = "Represents the data required for registering court return")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RequestForCourtTransferIn {

    @ApiModelProperty(required = true, value = "Agency identifier", example = "MDI", position = 1)
    @Length(max = 20, min = 2, message = "Agency identifier cannot be less then 2 and more than 20 characters")
    @NotNull
    private String agencyId;

    @ApiModelProperty(value = "Movement Reason Code", example = "CA", position = 2)
    @Length(max = 20, min = 1, message = "Movement reason code cannot be less then 2 and more than 20 characters")
    private String movementReasonCode;

    @ApiModelProperty(value = "Additional comments", example = "Prisoner was transferred to a new prison", position = 3)
    @Length(max = 240, message = "comment text size is a maximum of 240 characters")
    private String commentText;

    @ApiModelProperty(required = true, value = "The date and time the movement occurred, if not supplied it will be the current time", notes = "Time can be in the past but not before the last movement", position = 4, example = "2020-03-24T12:13:40")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateTime;

}
