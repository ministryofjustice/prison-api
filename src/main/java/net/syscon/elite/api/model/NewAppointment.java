package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Creation details for a new appointment
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Creation details for a new appointment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NewAppointment {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @Length(max=12) @Pattern(regexp="\\w*") @NotBlank
    private String appointmentType;

    @NotNull
    private Long locationId;

    @NotNull
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Length(max=4000) private String comment;

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
      * Corresponds to the scheduled event subType
      */
    @ApiModelProperty(required = true, value = "Corresponds to the scheduled event subType")
    @JsonProperty("appointmentType")
    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    /**
      * Location at which the appointment takes place.
      */
    @ApiModelProperty(required = true, value = "Location at which the appointment takes place.")
    @JsonProperty("locationId")
    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    /**
      * Date and time at which event starts
      */
    @ApiModelProperty(required = true, value = "Date and time at which event starts")
    @JsonProperty("startTime")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
      * Date and time at which event ends
      */
    @ApiModelProperty(value = "Date and time at which event ends")
    @JsonProperty("endTime")
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
      * Details of appointment
      */
    @ApiModelProperty(value = "Details of appointment")
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class NewAppointment {\n");
        
        sb.append("  appointmentType: ").append(appointmentType).append("\n");
        sb.append("  locationId: ").append(locationId).append("\n");
        sb.append("  startTime: ").append(startTime).append("\n");
        sb.append("  endTime: ").append(endTime).append("\n");
        sb.append("  comment: ").append(comment).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
