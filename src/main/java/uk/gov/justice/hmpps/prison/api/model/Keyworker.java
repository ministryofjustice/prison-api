package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Keyworker Details
 **/
@SuppressWarnings("unused")
@Schema(description = "Keyworker Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class Keyworker {
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

    public Keyworker(@NotNull Long staffId, @NotBlank String firstName, @NotBlank String lastName, @NotBlank String status, Long thumbnailId, @NotNull Integer numberAllocated) {
        this.staffId = staffId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.thumbnailId = thumbnailId;
        this.numberAllocated = numberAllocated;
    }

    public Keyworker() {
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
