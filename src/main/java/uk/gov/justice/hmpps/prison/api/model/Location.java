package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Location Details
 **/
@SuppressWarnings("unused")
@Schema(description = "Location Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class Location {
    @Schema(requiredMode = REQUIRED, description = "Location identifier.")
    @NotNull
    private Long locationId;

    @Schema(requiredMode = REQUIRED, description = "Location type.")
    @NotBlank
    private String locationType;

    @Schema(requiredMode = REQUIRED, description = "Location description.")
    @NotBlank
    private String description;

    @Schema(description = "What events this room can be used for.")
    private String locationUsage;

    @Schema(requiredMode = REQUIRED, description = "Identifier of Agency this location is associated with.")
    @NotBlank
    private String agencyId;

    @Schema(description = "Identifier of this location's parent location.")
    private Long parentLocationId;

    @Schema(description = "Current occupancy of location.")
    private Integer currentOccupancy;

    @Schema(description = "Location prefix. Defines search prefix that will constrain search to this location and its subordinate locations.")
    private String locationPrefix;

    @Schema(description = "Operational capacity of the location.")
    private Integer operationalCapacity;

    @Schema(description = "User-friendly location description.")
    private String userDescription;

    private String internalLocationCode;

    @Schema(description = "Indicates that sub locations exist for this location e.g. landings or cells")
    private Boolean subLocations;

    public Location(@NotNull Long locationId, @NotBlank String locationType, @NotBlank String description, String locationUsage, @NotBlank String agencyId, Long parentLocationId, Integer currentOccupancy, String locationPrefix, Integer operationalCapacity, String userDescription, String internalLocationCode, Boolean subLocations) {
        this.locationId = locationId;
        this.locationType = locationType;
        this.description = description;
        this.locationUsage = locationUsage;
        this.agencyId = agencyId;
        this.parentLocationId = parentLocationId;
        this.currentOccupancy = currentOccupancy;
        this.locationPrefix = locationPrefix;
        this.operationalCapacity = operationalCapacity;
        this.userDescription = userDescription;
        this.internalLocationCode = internalLocationCode;
        this.subLocations =  subLocations;
    }

    public Location() {
    }
}
