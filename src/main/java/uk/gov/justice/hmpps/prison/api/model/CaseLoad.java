package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "Case Load")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode(of = "caseLoadId")
@Data
public class CaseLoad {
    @Schema(required = true, description = "Case Load ID", example = "MDI")
    @JsonProperty("caseLoadId")
    @NotBlank
    private String caseLoadId;

    @Schema(required = true, description = "Full description of the case load", example = "Moorland Closed (HMP & YOI)")
    @JsonProperty("description")
    @NotBlank
    private String description;

    @Schema(required = true, description = "Type of case load. Note: Reference Code CSLD_TYPE", example = "INST", allowableValues = {"COMM","INST","APP"})
    @JsonProperty("type")
    @NotBlank
    private String type;

    @Schema(description = "Functional Use of the case load", example = "GENERAL", allowableValues = {"GENERAL","ADMIN"})
    @JsonProperty("caseloadFunction")
    private String caseloadFunction;

    @Schema(required = true, description = "Indicates that this caseload in the context of a staff member is the current active", example = "false")
    @JsonProperty("currentlyActive")
    @NotBlank
    private boolean currentlyActive;

    public CaseLoad(@NotBlank String caseLoadId, @NotBlank String description, @NotBlank String type, String caseloadFunction, @NotBlank boolean currentlyActive) {
        this.caseLoadId = caseLoadId;
        this.description = description;
        this.type = type;
        this.caseloadFunction = caseloadFunction;
        this.currentlyActive = currentlyActive;
    }

    public CaseLoad() {
    }

    public String getDescription() {
        return LocationProcessor.formatLocation(description);
    }

    @JsonIgnore
    public boolean isAdminType() {
        return "ADMIN".equals(caseloadFunction);
    }
}
