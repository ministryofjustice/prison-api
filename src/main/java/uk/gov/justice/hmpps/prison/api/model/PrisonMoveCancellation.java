package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@ApiModel(description = "Captures what is needed for cancellation of a scheduled prison to prison move.")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PrisonMoveCancellation {

    @ApiModelProperty(value = "The reason code for cancellation of the move.", allowableValues = "ADMI, OCI, TRANS", required = true)
    @NotBlank(message = "The reason for cancelling must be provided.")
    @Size(max = 12, message = "Reason code must be a maximum of 12 characters.")
    private String reasonCode;
}
