package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Case Note Amendment
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Case Note Amendment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CaseNoteAmendment {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private LocalDateTime creationDateTime;

    @NotBlank
    private String authorName;

    @NotBlank
    private String additionalNoteText;

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
      * Date and Time of Case Note creation
      */
    @ApiModelProperty(required = true, value = "Date and Time of Case Note creation")
    @JsonProperty("creationDateTime")
    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    /**
      * Name of the user amending the case note (lastname, firstname)
      */
    @ApiModelProperty(required = true, value = "Name of the user amending the case note (lastname, firstname)")
    @JsonProperty("authorName")
    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
      * Additional Case Note Information
      */
    @ApiModelProperty(required = true, value = "Additional Case Note Information")
    @JsonProperty("additionalNoteText")
    public String getAdditionalNoteText() {
        return additionalNoteText;
    }

    public void setAdditionalNoteText(String additionalNoteText) {
        this.additionalNoteText = additionalNoteText;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class CaseNoteAmendment {\n");
        
        sb.append("  creationDateTime: ").append(creationDateTime).append("\n");
        sb.append("  authorName: ").append(authorName).append("\n");
        sb.append("  additionalNoteText: ").append(additionalNoteText).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
