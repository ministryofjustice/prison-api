package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.lang.Object;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "code",
    "reason",
    "detail"
})
public class ErrorResponseImpl implements ErrorResponse {
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("code")
  private int code;

  @JsonProperty("reason")
  private String reason;

  @JsonProperty("detail")
  private String detail;

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperties(Map<String, Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  @JsonProperty("code")
  public int getCode() {
    return this.code;
  }

  @JsonProperty("code")
  public void setCode(int code) {
    this.code = code;
  }

  @JsonProperty("reason")
  public String getReason() {
    return this.reason;
  }

  @JsonProperty("reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  @JsonProperty("detail")
  public String getDetail() {
    return this.detail;
  }

  @JsonProperty("detail")
  public void setDetail(String detail) {
    this.detail = detail;
  }
}
