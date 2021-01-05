package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@ApiModel(description = "Represents the data required for recalling a prisoner")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RequestToRecall {

    @ApiModelProperty(required = true, value = "The time the recall occurred, if not supplied it will be the current time", notes = "Time can be in the past but not before the last movement", position = 1, example = "2020-03-24T12:13:40")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime recallTime;

    @ApiModelProperty(value = "Cell location where recalled prisoner should be housed", example = "MDI-RECP", position = 3)
    @Length(max = 240, message = "Cell Location description cannot be more than 240 characters")
    private String cellLocation;

    @ApiModelProperty(value = "Require imprisonment status", example = "MDI-RECP", position = 4)
    @Length(max = 12, message = "Imprisonment status cannot be more than 12 characters")
    private String imprisonmentStatus;


}
