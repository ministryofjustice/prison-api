package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Assigned Living Unit
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Assigned Living Unit")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AssignedLivingUnit {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotBlank
    private String agencyId;

    @NotNull
    private Long locationId;

    @NotBlank
    private String description;

    @NotBlank
    private String agencyName;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * Agency Id
     */
    @ApiModelProperty(required = true, value = "Agency Id")
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(final String agencyId) {
        this.agencyId = agencyId;
    }

    /**
     * location Id
     */
    @ApiModelProperty(required = true, value = "location Id")
    @JsonProperty("locationId")
    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(final Long locationId) {
        this.locationId = locationId;
    }

    /**
     * Living Unit Desc
     */
    @ApiModelProperty(required = true, value = "Living Unit Desc")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Name of the agency where this living unit resides
     */
    @ApiModelProperty(required = true, value = "Name of the agency where this living unit resides")
    @JsonProperty("agencyName")
    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(final String agencyName) {
        this.agencyName = agencyName;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class AssignedLivingUnit {\n");

        sb.append("  agencyId: ").append(agencyId).append("\n");
        sb.append("  locationId: ").append(locationId).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  agencyName: ").append(agencyName).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
