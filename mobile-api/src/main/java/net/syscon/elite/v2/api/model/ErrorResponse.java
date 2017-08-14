package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.lang.Object;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "Generic Error Response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "reason",
        "detail"
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @JsonProperty("code")
    private int code;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("detail")
    private String detail;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("code")
    public int getCode() {
        return this.code;
    }

    @ApiModelProperty(value = "Numeric application error code (may use HTTP status code for some responses).", required = true, position = 1)
    @JsonProperty("code")
    public void setCode(int code) {
        this.code = code;
    }

    @JsonProperty("reason")
    public String getReason() {
        return this.reason;
    }

    @ApiModelProperty(value = "Brief reason for error condition.", required = true, position = 2)
    @JsonProperty("reason")
    public void setReason(String reason) {
        this.reason = reason;
    }

    @JsonProperty("detail")
    public String getDetail() {
        return this.detail;
    }

    @ApiModelProperty(value = "Detailed description of error condition (with possible remedies, if applicable).", position = 3)
    @JsonProperty("detail")
    public void setDetail(String detail) {
        this.detail = detail;
    }
}
