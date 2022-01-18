package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSize;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Update details for an existing adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdjudication {

    @ApiModelProperty(required = true, value = "When the incident took place", position = 2, example = "15-06-2020T09:03:11")
    @NotNull
    private LocalDateTime incidentTime;

    @ApiModelProperty(required = true, value = "The id to indicate where the incident took place", notes = "This will be an agency's internal location id", position = 4)
    @NotNull
    private Long incidentLocationId;

    @ApiModelProperty(required = true, value = "The adjudication statement", position = 5, example = "The offence involved ...")
    @NotNull
    @MaximumTextSize
    @Size(max = 4000)
    private String statement;

    @ApiModelProperty(value = "The list of offence codes the offender may be charged with", notes = "If this value is not specified then the existing offence codes will be kept", position = 6, example = "51:80,51:25A")
    private List<String> offenceCodes;

    @ApiModelProperty(value = "TODO", position = 8, example = "TODO")
    private List<String> victimStaffIds = new ArrayList<>();

    @ApiModelProperty(value = "TODO", position = 9, example = "TODO")
    private List<String> victimOffenderIds = new ArrayList<>();

    @ApiModelProperty(value = "TODO", position = 9, example = "TODO")
    private List<String> connectedOffenders = new ArrayList<>();
}
