package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Offender Charge")
@Data
@NoArgsConstructor
@JsonPropertyOrder({"statute", "offence", "most_serious", "charge_active", "severity_ranking", "result", "disposition", "convicted", "imprisonment_status", "band"})
@Builder
@AllArgsConstructor
public class Charge {

    @Schema(description = "Offender Charge Id", example = "1231231")
    @JsonIgnore
    private Long offenderChargeId;

    @Schema(description = "Statute", example = "{ \"code\": \"PL96\", \"desc\": \"Police Act 1996\" }")
    private CodeDescription statute;

    @Schema(description = "Offence", example = "{ \"code\": \"PL96001\", \"desc\": \"Assault a constable in the execution of his / her duty\" }")
    private CodeDescription offence;

    @Schema(description = "Number of Offences", example = "2")
    @JsonIgnore
    private Integer noOfOffences;

    @Schema(description = "Most Serious Offence ", example = "true")
    @JsonProperty("most_serious")
    private boolean mostSerious;

    @Schema(description = "Charge Active", example = "true")
    @JsonProperty("charge_active")
    private boolean chargeActive;

    @Schema(description = "Severity Ranking", example = "100")
    @JsonProperty("severity_ranking")
    private String severityRanking;

    @Schema(description = "Result", example = "{ \"code\": \"1002\", \"desc\": \"Imprisonment\" }")
    private CodeDescription result;

    @Schema(description = "Disposition", example = "{ \"code\": \"F\", \"desc\": \"Final\" }")
    private CodeDescription disposition;

    @Schema(description = "Convicted", example = "true")
    private boolean convicted;

    @Schema(description = "Imprisonment Status", example = "{ \"code\": \"UNK_SENT\", \"desc\": \"Unknown Sentenced\" }")
    @JsonProperty("imprisonment_status")
    private CodeDescription imprisonmentStatus;

    @Schema(description = "Band", example = "{ \"code\": \"1\", \"desc\": \"Sent-Determinate NonFine\" }")
    private CodeDescription band;


}
