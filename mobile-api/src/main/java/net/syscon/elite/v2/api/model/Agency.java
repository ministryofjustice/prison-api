package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.lang.Object;
import java.lang.String;
import java.util.Map;

@ApiModel(description = "Agency Details")
@JsonDeserialize(
        as = AgencyImpl.class
)
public interface Agency {
    Map<String, Object> getAdditionalProperties();

    @ApiModelProperty(hidden = true)
    void setAdditionalProperties(Map<String, Object> additionalProperties);

    String getAgencyId();

    @ApiModelProperty(value = "Unique identifier for agency.", required = true, position = 1)
    void setAgencyId(String agencyId);

    String getDescription();

    @ApiModelProperty(value = "Agency description.", required = true, position = 2)
    void setDescription(String description);

    String getAgencyType();

    @ApiModelProperty(value = "Agency type.", required = true, position = 3)
    void setAgencyType(String agencyType);
}
