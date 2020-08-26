package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@ApiModel(description = "Request move offender to cell swap")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestMoveToCellSwap {
    @ApiModelProperty(value = "The reason code for the move (from reason code domain CHG_HOUS_RSN) (defaults to ADM)", example = "ADM")
    private String reasonCode;

    @ApiModelProperty(value = "The date / time of the move (defaults to current)",  example = "2020-03-24T12:13:40")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateTime;
}
