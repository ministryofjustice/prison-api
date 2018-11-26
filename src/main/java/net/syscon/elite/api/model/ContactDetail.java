package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contacts Details for offender
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Contacts Details for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ContactDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    @NotNull
    @Builder.Default
    private List<Contact> nextOfKin = new ArrayList<Contact>();

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
      * List of next of kin
      */
    @ApiModelProperty(required = true, value = "List of next of kin")
    @JsonProperty("nextOfKin")
    public List<Contact> getNextOfKin() {
        return nextOfKin;
    }

    public void setNextOfKin(List<Contact> nextOfKin) {
        this.nextOfKin = nextOfKin;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class ContactDetail {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  nextOfKin: ").append(nextOfKin).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
