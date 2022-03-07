package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Case Load")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "caseLoadId")
@Data
public class CaseLoad {
    @ApiModelProperty(required = true, value = "Case Load ID", position = 1, example = "MDI")
    @JsonProperty("caseLoadId")
    @NotBlank
    private String caseLoadId;

    @ApiModelProperty(required = true, value = "Full description of the case load", position = 2, example = "Moorland Closed (HMP & YOI)")
    @JsonProperty("description")
    @NotBlank
    private String description;

    @ApiModelProperty(required = true, value = "Type of case load", notes = "Reference Code CSLD_TYPE", position = 3, example = "INST", allowableValues = "COMM,INST,APP")
    @JsonProperty("type")
    @NotBlank
    private String type;

    @ApiModelProperty(value = "Functional Use of the case load", position = 4, example = "GENERAL", allowableValues = "GENERAL,ADMIN")
    @JsonProperty("caseloadFunction")
    private String caseloadFunction;

    @ApiModelProperty(required = true, value = "Indicates that this caseload in the context of a staff member is the current active", example = "false")
    @JsonProperty("currentlyActive")
    @NotBlank
    private boolean currentlyActive;

    public String getDescription() {
        return LocationProcessor.formatLocation(description);
    }

    @JsonIgnore
    public boolean isAdminType() {
        return "ADMIN".equals(caseloadFunction);
    }
}
