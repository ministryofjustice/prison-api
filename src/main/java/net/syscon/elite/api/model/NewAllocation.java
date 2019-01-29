package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * New Allocation
 **/
@SuppressWarnings("unused")
@ApiModel(description = "New Allocation")
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

    @Length(max=1) @Pattern(regexp="[AM]") @NotBlank
    private String type;

    @Length(max=12) @Pattern(regexp="\\w*") private String reason;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Offender Booking Id
      */
    @ApiModelProperty(required = true, value = "Offender Booking Id")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Keyworker's staff Id
      */
    @ApiModelProperty(required = true, value = "Keyworker's staff Id")
    @JsonProperty("staffId")
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    /**
      * Whether auto or manual
      */
    @ApiModelProperty(required = true, value = "Whether auto or manual")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
      * Allocation reason
      */
    @ApiModelProperty(value = "Allocation reason")
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class NewAllocation {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  staffId: ").append(staffId).append("\n");
        sb.append("  type: ").append(type).append("\n");
        sb.append("  reason: ").append(reason).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
