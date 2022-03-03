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

@ApiModel(description = "Represents the data required receiving a prisoner under a new booking")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RequestForNewBooking {

    @ApiModelProperty(value = "Received Prison ID", example = "MDI", position = 1)
    @Length(max = 3, message = "Prison ID is 3 character code")
    @NotNull
    private String prisonId;

    @ApiModelProperty(required = true, value = "The time the booking in occurred, if not supplied it will be the current time", position = 2, example = "2020-03-24T12:13:40")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime bookingInTime;

    @ApiModelProperty(value = "Where the prisoner has moved from (default OUT)", example = "OUT", position = 3)
    @Length(max = 6, message = "From location")
    private String fromLocationId;

    @ApiModelProperty(value = "Reason for in movement (e.g. Unconvicted Remand)", example = "N", position = 4)
    @NotNull
    private String movementReasonCode;

    @ApiModelProperty(value = "Is this offender a youth", example = "false", position = 5)
    private boolean youthOffender;

    @ApiModelProperty(value = "Cell location where recalled prisoner should be housed, default will be reception", example = "MDI-RECP", position = 6)
    @Length(max = 240, message = "Cell Location description cannot be more than 240 characters")
    private String cellLocation;

    @ApiModelProperty(value = "Require imprisonment status (e.g Adult Imprisonment Without Option CJA03)", example = "SENT03", position = 7)
    @Length(max = 12, message = "Imprisonment status cannot be more than 12 characters")
    private String imprisonmentStatus;


}
