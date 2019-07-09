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
@JsonPropertyOrder({"reception_arrival_date_and_time", "status", "imprisonment_status"})
public class PssSentenceData {

    @ApiModelProperty(value = "Reception arrival date and time", name = "reception_arrival_date_and_time", example = "2019-05-03 15:50:00", position = 0)
    @JsonProperty("reception_arrival_date_and_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receptionDateTime;

    @ApiModelProperty(value = "Conviction status", name = "status", example = "Convicted", position = 1)
    @JsonProperty("status")
    private String convictionStatus;

    @ApiModelProperty(value = "Imprisonment status code and description", name = "imprisonment_status", position = 2)
    @JsonProperty("imprisonment_status")
    private CodeDescription imprisonmentStatus;
}
