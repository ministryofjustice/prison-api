package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.util.SortAttribute;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Establishment roll count numbers for a housing block, wing, or reception etc.
 **/
@SuppressWarnings("unused")
@Schema(description = "Establishment roll count numbers for a housing block, wing, or reception etc.")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RollCount implements SortAttribute {

    @Schema(requiredMode = REQUIRED, description = "Id of location")
    @NotNull
    private Long livingUnitId;

    @Schema(requiredMode = REQUIRED, description = "Type of location", example = "WING")
    @NotBlank
    private String locationType;

    @Schema(requiredMode = REQUIRED, description = "Unique code for this location within the prison", example = "001")
    @NotBlank
    private String locationCode;

    @Schema(requiredMode = REQUIRED, description = "Unique code for this location", example = "MDI-1-1-001")
    @NotBlank
    private String fullLocationPath;

    @Schema(requiredMode = REQUIRED, description = "Wing, House block name or landing code, blank for cell")
    @NotBlank
    private String livingUnitDesc;

    @Schema(requiredMode = REQUIRED, description = "Location Id of the parent location. This will be NULL for top level locations (e.g. Wings)")
    private Long parentLocationId;

    @Schema(description = "Parent type of location", example = "WING")
    private String parentLocationType;

    @Schema(description = "Parent type of location", example = "A")
    private String parentLocationCode;

    @Schema(description = "Parent unique code for this location", example = "A")
    private String parentFullLocationPath;

    @Schema(description = "The locally used name for a wing or landing", example = "Wing A")
    private String parentLocalName;

    @Schema(requiredMode = REQUIRED, description = "No of residential prisoners")
    @NotNull
    private Integer bedsInUse;

    @Schema(requiredMode = REQUIRED, description = "No of residential prisoners actually in")
    @NotNull
    private Integer currentlyInCell;

    @Schema(requiredMode = REQUIRED, description = "No of residential prisoners in internal locations")
    @NotNull
    private Integer outOfLivingUnits;

    @Schema(requiredMode = REQUIRED, description = "No of residential prisoners out")
    @NotNull
    private Integer currentlyOut;

    @Schema(requiredMode = REQUIRED, description = "Total capacity not including unavailable cells")
    @NotNull
    private Integer operationalCapacity;

    @Schema(requiredMode = REQUIRED, description = "Available empty beds")
    @NotNull
    private Integer netVacancies;

    @Schema(requiredMode = REQUIRED, description = "Total capacity including unavailable cells")
    @NotNull
    private Integer maximumCapacity;

    @Schema(requiredMode = REQUIRED, description = "All empty beds")
    @NotNull
    private Integer availablePhysical;

    @Schema(requiredMode = REQUIRED, description = "No of unavailable cells")
    @NotNull
    private Integer outOfOrder;

    @org.jetbrains.annotations.NotNull
    @Override
    public String getKey() {
        return getLivingUnitDesc();
    }
}
