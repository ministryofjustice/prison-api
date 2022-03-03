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

/**
 * Location Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Location Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Location {
    @ApiModelProperty(required = true, value = "Location identifier.")
    @NotNull
    private Long locationId;

    @ApiModelProperty(required = true, value = "Location type.")
    @NotBlank
    private String locationType;

    @ApiModelProperty(required = true, value = "Location description.")
    @NotBlank
    private String description;

    @ApiModelProperty(value = "What events this room can be used for.")
    private String locationUsage;

    @ApiModelProperty(required = true, value = "Identifier of Agency this location is associated with.")
    @NotBlank
    private String agencyId;

    @ApiModelProperty(value = "Identifier of this location's parent location.")
    private Long parentLocationId;

    @ApiModelProperty(value = "Current occupancy of location.")
    private Integer currentOccupancy;

    @ApiModelProperty(value = "Location prefix. Defines search prefix that will constrain search to this location and its subordinate locations.")
    private String locationPrefix;

    @ApiModelProperty(value = "Operational capacity of the location.")
    private Integer operationalCapacity;

    @ApiModelProperty(value = "User-friendly location description.")
    private String userDescription;

    private String internalLocationCode;
}
