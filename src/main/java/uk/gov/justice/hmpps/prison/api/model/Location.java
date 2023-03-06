package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Location Details
 **/
@SuppressWarnings("unused")
@Schema(description = "Location Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class Location {
    @Schema(required = true, description = "Location identifier.")
    @NotNull
    private Long locationId;

    @Schema(required = true, description = "Location type.")
    @NotBlank
    private String locationType;

    @Schema(required = true, description = "Location description.")
    @NotBlank
    private String description;

    @Schema(description = "What events this room can be used for.")
    private String locationUsage;

    @Schema(required = true, description = "Identifier of Agency this location is associated with.")
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

    public Location(@NotNull Long locationId, @NotBlank String locationType, @NotBlank String description, String locationUsage, @NotBlank String agencyId, Long parentLocationId, Integer currentOccupancy, String locationPrefix, Integer operationalCapacity, String userDescription, String internalLocationCode) {
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
    }

    public Location() {
    }
}
