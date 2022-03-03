package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

/**
 * New Allocation
 **/
@SuppressWarnings("unused")
@Schema(description = "New Allocation")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NewAllocation {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    private Long bookingId;

    @NotNull
    private Long staffId;

    @Size(max = 1)
    @Pattern(regexp = "[AM]")
    @NotBlank
    private String type;

    @Size(max = 12)
    @Pattern(regexp = "\\w*")
    private String reason;

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
     * Offender Booking Id
     */
    @Schema(required = true, description = "Offender Booking Id")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(final Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
     * Keyworker's staff Id
     */
    @Schema(required = true, description = "Keyworker's staff Id")
    @JsonProperty("staffId")
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(final Long staffId) {
        this.staffId = staffId;
    }

    /**
     * Whether auto or manual
     */
    @Schema(required = true, description = "Whether auto or manual")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Allocation reason
     */
    @Schema(description = "Allocation reason")
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class NewAllocation {\n");

        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  staffId: ").append(staffId).append("\n");
        sb.append("  type: ").append(type).append("\n");
        sb.append("  reason: ").append(reason).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
