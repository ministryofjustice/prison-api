package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel(description = "Offence Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenceDetail {

    @ApiModelProperty(required = true, value = "Prisoner booking id", example = "1123456", position = 1)
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Description of offence", position = 2)
    @NotBlank
    private String offenceDescription;

    @ApiModelProperty(required = true, value = "Reference Code", example = "RR84070", position = 3)
    @NotBlank
    private String offenceCode;

    @ApiModelProperty(required = true, value = "Statute code", example = "RR84", position = 4)
    @NotBlank
    private String statuteCode;
}
