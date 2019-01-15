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
import java.util.HashMap;
import java.util.Map;

/**
 * Attendance details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Attendance details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UpdateAttendance {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @Length(max=12) @NotBlank
    private String eventOutcome;

    @Length(max=12) private String performance;

    @Length(max=240) private String outcomeComment;

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
      * Attendance outcome, possible values are the codes in the 'PS_PA_OC' reference domain.
      */
    @ApiModelProperty(required = true, value = "Attendance outcome, possible values are the codes in the 'PS_PA_OC' reference domain.")
    @JsonProperty("eventOutcome")
    public String getEventOutcome() {
        return eventOutcome;
    }

    public void setEventOutcome(String eventOutcome) {
        this.eventOutcome = eventOutcome;
    }

    /**
      * Possible values are the codes in the 'PERFORMANCE' reference domain, mandatory for eventOutcome 'ATT'.
      */
    @ApiModelProperty(value = "Possible values are the codes in the 'PERFORMANCE' reference domain, mandatory for eventOutcome 'ATT'.")
    @JsonProperty("performance")
    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    /**
      * Free text comment, maximum length 240 characters.
      */
    @ApiModelProperty(value = "Free text comment, maximum length 240 characters.")
    @JsonProperty("outcomeComment")
    public String getOutcomeComment() {
        return outcomeComment;
    }

    public void setOutcomeComment(String outcomeComment) {
        this.outcomeComment = outcomeComment;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class UpdateAttendance {\n");
        
        sb.append("  eventOutcome: ").append(eventOutcome).append("\n");
        sb.append("  performance: ").append(performance).append("\n");
        sb.append("  outcomeComment: ").append(outcomeComment).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
