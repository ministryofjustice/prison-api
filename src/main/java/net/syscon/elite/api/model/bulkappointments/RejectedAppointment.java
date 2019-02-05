package net.syscon.elite.api.model.bulkappointments;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import net.syscon.elite.api.model.ErrorResponse;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Detail for a rejected appointment request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class RejectedAppointment {
    @ApiModelProperty(required = true, value = "Reified appointment detail from the request")
    @NotNull
    private AppointmentDetails appointmentDetails;

    @ApiModelProperty(required = true, value = "The reason for rejecting the appointment request, expressed as an ErrorResponse")
    @NotNull
    private ErrorResponse errorResponse;

}
