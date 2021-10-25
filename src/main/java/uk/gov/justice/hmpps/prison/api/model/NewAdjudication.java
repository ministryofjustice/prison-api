package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSize;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@ApiModel(description = "Creation details for a new adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewAdjudication {

    @ApiModelProperty(required = true, value = "Booking number", position = 1, example = "123456")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "When the incident took place", position = 2, example = "15-06-2020T09:03:11")
    @NotNull
    private LocalDateTime incidentTime;

    @ApiModelProperty(required = true, value = "The id to indicate where the incident took place", notes = "This will be an agency's internal location id", position = 3)
    @NotNull
    private Long incidentLocationId;

    @ApiModelProperty(required = true, value = "The adjudication statement", position = 4, example = "The offence involved ...")
    @NotNull
    @MaximumTextSize
    @Size(max = 4000)
    private String statement;
}
