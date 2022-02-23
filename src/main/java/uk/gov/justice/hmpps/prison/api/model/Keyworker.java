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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Keyworker Details
 **/
@SuppressWarnings("unused")
@Schema(description = "Keyworker Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Keyworker {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    private Long staffId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String status;

    private Long thumbnailId;

    @NotNull
    private Integer numberAllocated;

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
     * Unique identifier for staff member.
     */
    @Schema(required = true, description = "Unique identifier for staff member.")
    @JsonProperty("staffId")
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(final Long staffId) {
        this.staffId = staffId;
    }

    /**
     * Staff member's first name.
     */
    @Schema(required = true, description = "Staff member's first name.")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * Staff member's last name.
     */
    @Schema(required = true, description = "Staff member's last name.")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * Status of staff member.
     */
    @Schema(required = true, description = "Status of staff member.")
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Identifier for staff member image.
     */
    @Schema(description = "Identifier for staff member image.")
    @JsonProperty("thumbnailId")
    public Long getThumbnailId() {
        return thumbnailId;
    }

    public void setThumbnailId(final Long thumbnailId) {
        this.thumbnailId = thumbnailId;
    }

    /**
     * Current number allocated
     */
    @Schema(required = true, description = "Current number allocated")
    @JsonProperty("numberAllocated")
    public Integer getNumberAllocated() {
        return numberAllocated;
    }

    public void setNumberAllocated(final Integer numberAllocated) {
        this.numberAllocated = numberAllocated;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class Keyworker {\n");

        sb.append("  staffId: ").append(staffId).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  status: ").append(status).append("\n");
        sb.append("  thumbnailId: ").append(thumbnailId).append("\n");
        sb.append("  numberAllocated: ").append(numberAllocated).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
