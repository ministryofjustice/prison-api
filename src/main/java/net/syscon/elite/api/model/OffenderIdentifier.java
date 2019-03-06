package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Offender Identifier
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Offender Identifier")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OffenderIdentifier {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String type;

    @NotBlank
    private String value;

    private String issuedAuthorityText;

    private LocalDate issuedDate;

    private String caseloadType;

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
      * Type of offender identifier
      */
    @ApiModelProperty(required = true, value = "Type of offender identifier")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
      * The value of the offender identifier
      */
    @ApiModelProperty(required = true, value = "The value of the offender identifier")
    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    /**
      * Issuing Authority Information
      */
    @ApiModelProperty(value = "Issuing Authority Information")
    @JsonProperty("issuedAuthorityText")
    public String getIssuedAuthorityText() {
        return issuedAuthorityText;
    }

    public void setIssuedAuthorityText(final String issuedAuthorityText) {
        this.issuedAuthorityText = issuedAuthorityText;
    }

    /**
      * Date of issue
      */
    @ApiModelProperty(value = "Date of issue")
    @JsonProperty("issuedDate")
    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(final LocalDate issuedDate) {
        this.issuedDate = issuedDate;
    }

    /**
      * Related caseload type
      */
    @ApiModelProperty(value = "Related caseload type")
    @JsonProperty("caseloadType")
    public String getCaseloadType() {
        return caseloadType;
    }

    public void setCaseloadType(final String caseloadType) {
        this.caseloadType = caseloadType;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class OffenderIdentifier {\n");
        
        sb.append("  type: ").append(type).append("\n");
        sb.append("  value: ").append(value).append("\n");
        sb.append("  issuedAuthorityText: ").append(issuedAuthorityText).append("\n");
        sb.append("  issuedDate: ").append(issuedDate).append("\n");
        sb.append("  caseloadType: ").append(caseloadType).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
