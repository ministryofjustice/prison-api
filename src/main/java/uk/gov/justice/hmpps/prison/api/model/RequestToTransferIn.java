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

@ApiModel(description = "Represents the data required for receiving a prisoner transfer")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RequestToTransferIn {

    @ApiModelProperty(required = true, value = "The time the movement occurred, if not supplied it will be the current time", notes = "Time can be in the past but not before the last movement", position = 1, example = "2020-03-24T12:13:40")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime receiveTime;

    @ApiModelProperty(value = "Additional comments about the release", example = "Prisoner was transferred to a new prison", position = 2)
    @Length(max = 240, message = "Comments size is a maximum of 240 characters")
    private String commentText;

    @ApiModelProperty(value = "Cell location", example = "MDI-RECP", position = 3)
    @Length(max = 240, message = "Cell Location description cannot be more than 240 characters")
    private String cellLocation;


}
