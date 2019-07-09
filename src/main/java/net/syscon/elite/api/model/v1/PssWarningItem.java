package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"warning_type", "warning_sub_type", "warning_date", "status"})
public class PssWarningItem {

    @ApiModelProperty(value = "Warning type code and description", name = "warning_type", position = 0)
    @JsonProperty("warning_type")
    private CodeDescription warningType;

    @ApiModelProperty(value = "Warning sub-type code and description", name = "warning_sub_type", position = 1)
    @JsonProperty("warning_sub_type")
    private CodeDescription warningSubType;

    @ApiModelProperty(value = "Date the warning was issued", name = "warning_date", example = "2015-06-03 00:00:00", position = 2)
    @JsonProperty("warning_date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime warningDateTime;

    @ApiModelProperty(value = "Warning status", name = "warning_status", example = "ACTIVE", position = 3)
    @JsonProperty("status")
    private String status;
}