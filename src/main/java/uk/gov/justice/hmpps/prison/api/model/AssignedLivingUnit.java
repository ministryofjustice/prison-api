package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Assigned Living Unit
 **/
@SuppressWarnings("unused")
@Schema(description = "Assigned Living Unit")
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

    @Hidden
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * Agency Id
     */
    @Schema(description = "Agency Id", requiredMode = RequiredMode.NOT_REQUIRED)
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
    @Schema(description = "location Id", requiredMode = RequiredMode.NOT_REQUIRED)
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
    @Schema(description = "Living Unit Desc", requiredMode = RequiredMode.NOT_REQUIRED)
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
    @Schema(description = "Name of the agency where this living unit resides", requiredMode = RequiredMode.NOT_REQUIRED)
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
