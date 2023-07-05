package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Cell Locations are grouped for unlock lists as a 2 level tree. The two levels are referred to as Location and Sub-Location in the digital prison services UI. Each (location/sub-location) group has a name that is understood by prison officers and also serves as a key to retrieve the corresponding Cell Locations and information about their occupants.
 **/
@SuppressWarnings("unused")
@Schema(description = "Cell Locations are grouped for unlock lists as a 2 level tree. The two levels are referred to as Location and Sub-Location in the digital prison services UI. Each (location/sub-location) group has a name that is understood by prison officers and also serves as a key to retrieve the corresponding Cell Locations and information about their occupants.")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class LocationGroup {
    @Schema(requiredMode = REQUIRED, description = "The name of the group")
    @NotBlank
    private String name;

    @Schema(requiredMode = REQUIRED, description = "A key for the group")
    @NotBlank
    private String key;

    @Schema(requiredMode = REQUIRED, description = "The child groups of this group")
    @NotNull
    @Builder.Default
    private List<LocationGroup> children = new ArrayList<>();
}

