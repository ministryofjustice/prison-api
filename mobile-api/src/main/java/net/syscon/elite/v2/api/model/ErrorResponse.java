package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.lang.Object;
import java.lang.String;
import java.util.Map;

@ApiModel(description = "Generic Error Response")
@JsonDeserialize(
        as = ErrorResponseImpl.class
)
public interface ErrorResponse {
    Map<String, Object> getAdditionalProperties();

    @ApiModelProperty(hidden = true)
    void setAdditionalProperties(Map<String, Object> additionalProperties);

    int getCode();

    @ApiModelProperty(value = "Numeric application error code (may use HTTP status code for some responses).", required = true, position = 1)
    void setCode(int code);

    String getReason();

    @ApiModelProperty(value = "Brief reason for error condition.", required = true, position = 2)
    void setReason(String reason);

    String getDetail();

    @ApiModelProperty(value = "Detailed description of error condition (with possible remedies, if applicable).", position = 3)
    void setDetail(String detail);
}
