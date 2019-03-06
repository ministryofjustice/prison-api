package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
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
 * Offence Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Offence Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OffenceDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    @NotBlank
    private String offenceDescription;

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
      * Offender booking id.
      */
    @ApiModelProperty(required = true, value = "Offender booking id.")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(final Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Description of offence.
      */
    @ApiModelProperty(required = true, value = "Description of offence.")
    @JsonProperty("offenceDescription")
    public String getOffenceDescription() {
        return offenceDescription;
    }

    public void setOffenceDescription(final String offenceDescription) {
        this.offenceDescription = offenceDescription;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class OffenceDetail {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  offenceDescription: ").append(offenceDescription).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
