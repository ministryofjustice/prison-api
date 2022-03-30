package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Location Count
 **/
@SuppressWarnings("unused")
@Schema(description = "Location Count")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class LocationCount {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    private Long physicalCountId;

    @NotNull
    private Long locationId;

    private Long enteredByUserId;

    @NotNull
    private LocationCountDetail initialCount;

    private LocationCountDetail recount;

    public enum CountStatusCode {
        I, R, C,
    }

    private CountStatusCode countStatusCode;

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
     * Phyical Count Id
     */
    @Schema(required = true, description = "Phyical Count Id")
    @JsonProperty("physicalCountId")
    public Long getPhysicalCountId() {
        return physicalCountId;
    }

    public void setPhysicalCountId(final Long physicalCountId) {
        this.physicalCountId = physicalCountId;
    }

    /**
     * Location Id
     */
    @Schema(required = true, description = "Location Id")
    @JsonProperty("locationId")
    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(final Long locationId) {
        this.locationId = locationId;
    }

    /**
     * Staff Id
     */
    @Schema(description = "Staff Id")
    @JsonProperty("enteredByUserId")
    public Long getEnteredByUserId() {
        return enteredByUserId;
    }

    public void setEnteredByUserId(final Long enteredByUserId) {
        this.enteredByUserId = enteredByUserId;
    }

    /**
     * Initial Count Details
     */
    @Schema(required = true, description = "Initial Count Details")
    @JsonProperty("initialCount")
    public LocationCountDetail getInitialCount() {
        return initialCount;
    }

    public void setInitialCount(final LocationCountDetail initialCount) {
        this.initialCount = initialCount;
    }

    /**
     * Recount Details
     */
    @Schema(description = "Recount Details")
    @JsonProperty("recount")
    public LocationCountDetail getRecount() {
        return recount;
    }

    public void setRecount(final LocationCountDetail recount) {
        this.recount = recount;
    }

    /**
     * Status of count
     */
    @Schema(description = "Status of count")
    @JsonProperty("countStatusCode")
    public CountStatusCode getCountStatusCode() {
        return countStatusCode;
    }

    public void setCountStatusCode(final CountStatusCode countStatusCode) {
        this.countStatusCode = countStatusCode;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class LocationCount {\n");

        sb.append("  physicalCountId: ").append(physicalCountId).append("\n");
        sb.append("  locationId: ").append(locationId).append("\n");
        sb.append("  enteredByUserId: ").append(enteredByUserId).append("\n");
        sb.append("  initialCount: ").append(initialCount).append("\n");
        sb.append("  recount: ").append(recount).append("\n");
        sb.append("  countStatusCode: ").append(countStatusCode).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
