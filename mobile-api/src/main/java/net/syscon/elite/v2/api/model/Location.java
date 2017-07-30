package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@ApiModel(description = "Location Details")
@JsonDeserialize(
        as = LocationImpl.class
)
public interface Location {
    Map<String, Object> getAdditionalProperties();

    @ApiModelProperty(hidden = true)
    void setAdditionalProperties(Map<String, Object> additionalProperties);

    int getLocationId();

    @ApiModelProperty(value = "Unique identifier for location.", required = true, position = 1)
    void setLocationId(int locationId);

    String getLocationType();

    @ApiModelProperty(value = "Location type.", required = true, position = 2)
    void setLocationType(String locationType);

    String getDescription();

    @ApiModelProperty(value = "Location description.", required = true, position = 3)
    void setDescription(String description);

    int getParentLocationId();

    @ApiModelProperty(value = "Identifier of this location's parent location.", position = 4)
    void setParentLocationId(int parentLocationId);

    int getCurrentOccupancy();

    @ApiModelProperty(value = "Current occupancy of location.", position = 5)
    void setCurrentOccupancy(int currentOccupancy);
}
