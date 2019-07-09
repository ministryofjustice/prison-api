package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@ApiModel(description = "Offender Event")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonInclude(Include.NON_NULL)
public class Event {

    @ApiModelProperty(value = "Type of event", required = true, example = "IEP_CHANGED", position = 1)
    private String type;
    @ApiModelProperty(value = "Unique indentifier for event", required = true, example = "21", position = 2)
    private Long id;
    @ApiModelProperty(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true, position = 3)
    private String nomsId;
    @ApiModelProperty(name = "prison_id", value = "Prison ID", example = "BMI", required = true, position = 4)
    private String prisonId;
    @ApiModelProperty(name = "timestamp", value = "Daten and time the event occurred", example = "2016-10-21 15:55:06.284", required = true, position = 5)
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    @JsonRawValue
    private String eventData;
}
