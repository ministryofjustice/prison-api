package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel(description = "Location Details")
@JsonDeserialize(
        as = LocationImpl.class
)
public interface Location {
    Map<String, Object> getAdditionalProperties();

    @ApiModelProperty(hidden = true)
    void setAdditionalProperties(Map<String, Object> additionalProperties);

    Long getLocationId();

    @ApiModelProperty(value = "Unique identifier for location.", required = true, position = 1)
    @NotNull
    void setLocationId(Long locationId);

    String getLocationType();

    @ApiModelProperty(value = "Location type.", required = true, position = 2)
    @NotNull
    @NotBlank
    void setLocationType(String locationType);

    String getDescription();

    @ApiModelProperty(value = "Location description.", required = true, position = 3)
    @NotNull
    @NotBlank
    void setDescription(String description);

    String getAgencyId();

    @ApiModelProperty(value = "Agency this location is associated with.", required = true, position = 4)
    @NotNull
    @NotBlank
    void setAgencyId(String agencyId);

    Long getParentLocationId();

    @ApiModelProperty(value = "Identifier of this location's parent location.", position = 5)
    void setParentLocationId(Long parentLocationId);

    Integer getCurrentOccupancy();

    @ApiModelProperty(value = "Current occupancy of location.", position = 6)
    void setCurrentOccupancy(Integer currentOccupancy);
}
