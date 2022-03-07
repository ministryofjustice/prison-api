package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * Location Inmate Count
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Location Inmate Count")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class LocationCountDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    private Long conductByUserId;

    @NotNull
    private Long enteredByUserId;

    @NotNull
    private Integer inmateCount;

    @NotBlank
    private String countReasonCode;

    private String comment;

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
     * Who did the count (staff ID)
     */
    @ApiModelProperty(required = true, value = "Who did the count (staff ID)")
    @JsonProperty("conductByUserId")
    public Long getConductByUserId() {
        return conductByUserId;
    }

    public void setConductByUserId(final Long conductByUserId) {
        this.conductByUserId = conductByUserId;
    }

    /**
     * Staff Id
     */
    @ApiModelProperty(required = true, value = "Staff Id")
    @JsonProperty("enteredByUserId")
    public Long getEnteredByUserId() {
        return enteredByUserId;
    }

    public void setEnteredByUserId(final Long enteredByUserId) {
        this.enteredByUserId = enteredByUserId;
    }

    /**
     * The number of inmates
     */
    @ApiModelProperty(required = true, value = "The number of inmates")
    @JsonProperty("inmateCount")
    public Integer getInmateCount() {
        return inmateCount;
    }

    public void setInmateCount(final Integer inmateCount) {
        this.inmateCount = inmateCount;
    }

    /**
     * Reason for count
     */
    @ApiModelProperty(required = true, value = "Reason for count")
    @JsonProperty("countReasonCode")
    public String getCountReasonCode() {
        return countReasonCode;
    }

    public void setCountReasonCode(final String countReasonCode) {
        this.countReasonCode = countReasonCode;
    }

    /**
     * Comments
     */
    @ApiModelProperty(value = "Comments")
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class LocationCountDetail {\n");

        sb.append("  conductByUserId: ").append(conductByUserId).append("\n");
        sb.append("  enteredByUserId: ").append(enteredByUserId).append("\n");
        sb.append("  inmateCount: ").append(inmateCount).append("\n");
        sb.append("  countReasonCode: ").append(countReasonCode).append("\n");
        sb.append("  comment: ").append(comment).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
