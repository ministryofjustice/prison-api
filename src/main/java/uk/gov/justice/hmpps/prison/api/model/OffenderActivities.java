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
import java.util.List;


@ApiModel(description = "Information about an Offender's activities")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderActivities {
    @NotBlank
    @ApiModelProperty(required = true, value = "Display Prisoner Number (UK is NOMS ID)")
    private String offenderNo;

    @ApiModelProperty(value = "The current work activities")
    @NotNull
    private List<OffenderActivitySummary> workActivities;
}
