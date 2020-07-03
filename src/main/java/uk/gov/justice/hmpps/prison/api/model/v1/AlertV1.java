package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@ApiModel(description = "Offender Alert")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"alert_type", "alert_sub_type", "alert_date", "expiry_date", "status", "comment"})
public class AlertV1 {

    @ApiModelProperty(value = "Code and description identifying the type of alert", required = true, example = "{ \"code\": \"FX\", \"desc\": \"Security\" }")
    @JsonProperty("alert_type")
    private CodeDescription type;

    @ApiModelProperty(value = "Code and description identifying the sub type of alert", position = 1, required = true, example = "{ \"code\": \"XEL\", \"desc\": \"Escape List\" }")
    @JsonProperty("alert_sub_type")
    private CodeDescription subType;

    @ApiModelProperty(value = "Date the alert became effective", position = 2, example = "2019-02-13", required = true)
    @JsonProperty("alert_date")
    private LocalDate date;

    @ApiModelProperty(value = "Alert Type", position = 3, example = "2019-04-15")
    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @ApiModelProperty(value = "ACTIVE or INACTIVE (Inactive alerts will have a expiry date of today or earlier", position = 4, example = "ACTIVE", allowableValues = "ACTIVE,INACTIVE")
    private String status;

    @ApiModelProperty(value = "Free Text Comment", position = 5, example = "has a large poster on cell wall")
    private String comment;

    @JsonIgnore
    private int seq;


}
