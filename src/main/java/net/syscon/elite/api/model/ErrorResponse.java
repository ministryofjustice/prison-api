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
 * General API Error Response
 **/
@SuppressWarnings("unused")
@ApiModel(description = "General API Error Response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ErrorResponse {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Integer status;

    private Integer errorCode;

    @NotBlank
    private String userMessage;

    private String developerMessage;

    private String moreInfo;

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
      * Response status code (will typically mirror HTTP status code).
      */
    @ApiModelProperty(required = true, value = "Response status code (will typically mirror HTTP status code).")
    @JsonProperty("status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(final Integer status) {
        this.status = status;
    }

    /**
      * An (optional) application-specific error code.
      */
    @ApiModelProperty(value = "An (optional) application-specific error code.")
    @JsonProperty("errorCode")
    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
      * Concise error reason for end-user consumption.
      */
    @ApiModelProperty(required = true, value = "Concise error reason for end-user consumption.")
    @JsonProperty("userMessage")
    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(final String userMessage) {
        this.userMessage = userMessage;
    }

    /**
      * Detailed description of problem with remediation hints aimed at application developer.
      */
    @ApiModelProperty(value = "Detailed description of problem with remediation hints aimed at application developer.")
    @JsonProperty("developerMessage")
    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(final String developerMessage) {
        this.developerMessage = developerMessage;
    }

    /**
      * Provision for further information about the problem (e.g. a link to a FAQ or knowledge base article).
      */
    @ApiModelProperty(value = "Provision for further information about the problem (e.g. a link to a FAQ or knowledge base article).")
    @JsonProperty("moreInfo")
    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(final String moreInfo) {
        this.moreInfo = moreInfo;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class ErrorResponse {\n");
        
        sb.append("  status: ").append(status).append("\n");
        sb.append("  errorCode: ").append(errorCode).append("\n");
        sb.append("  userMessage: ").append(userMessage).append("\n");
        sb.append("  developerMessage: ").append(developerMessage).append("\n");
        sb.append("  moreInfo: ").append(moreInfo).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
