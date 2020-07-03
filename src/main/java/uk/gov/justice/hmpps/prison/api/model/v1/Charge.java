package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Offender Charge")
@Data
@NoArgsConstructor
@JsonPropertyOrder({"statute", "offence", "most_serious", "charge_active", "severity_ranking", "result", "disposition", "convicted", "imprisonment_status", "band"})
@Builder
@AllArgsConstructor
public class Charge {

    @ApiModelProperty(value = "Offender Charge Id", position = 0, example = "1231231")
    @JsonIgnore
    private Long offenderChargeId;

    @ApiModelProperty(value = "Statute", position = 1, example = "{ \"code\": \"PL96\", \"desc\": \"Police Act 1996\" }")
    private CodeDescription statute;

    @ApiModelProperty(value = "Offence", position = 2, example = "{ \"code\": \"PL96001\", \"desc\": \"Assault a constable in the execution of his / her duty\" }")
    private CodeDescription offence;

    @ApiModelProperty(value = "Number of Offences", position = 3, example = "2")
    @JsonIgnore
    private Integer noOfOffences;

    @ApiModelProperty(value = "Most Serious Offence ", position = 4, example = "true")
    @JsonProperty("most_serious")
    private boolean mostSerious;

    @ApiModelProperty(value = "Charge Active", position = 5, example = "true")
    @JsonProperty("charge_active")
    private boolean chargeActive;

    @ApiModelProperty(value = "Severity Ranking", position = 6, example = "100")
    @JsonProperty("severity_ranking")
    private String severityRanking;

    @ApiModelProperty(value = "Result", position = 7, example = "{ \"code\": \"1002\", \"desc\": \"Imprisonment\" }")
    private CodeDescription result;

    @ApiModelProperty(value = "Disposition", position = 8, example = "{ \"code\": \"F\", \"desc\": \"Final\" }")
    private CodeDescription disposition;

    @ApiModelProperty(value = "Convicted", position = 9, example = "true")
    private boolean convicted;

    @ApiModelProperty(value = "Imprisonment Status", position = 10, example = "{ \"code\": \"UNK_SENT\", \"desc\": \"Unknown Sentenced\" }")
    @JsonProperty("imprisonment_status")
    private CodeDescription imprisonmentStatus;

    @ApiModelProperty(value = "Band", position = 11, example = "{ \"code\": \"1\", \"desc\": \"Sent-Determinate NonFine\" }")
    private CodeDescription band;


}
