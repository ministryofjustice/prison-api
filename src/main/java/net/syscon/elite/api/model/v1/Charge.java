package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonPropertyOrder({"statute", "offence", "most_serious", "charge_active", "severity_ranking", "result", "disposition", "convicted", "imprisonment_status", "band"})
public class Charge {

    @JsonIgnore
    private Long offenderChargeId;
    @JsonProperty("statute")
    private CodeDescription statute;
    @JsonProperty("offence")
    private CodeDescription offence;
    @JsonIgnore
    private Integer noOfOffences;
    @JsonProperty("most_serious")
    private boolean mostSerious;
    @JsonProperty("charge_active")
    private boolean chargeActive;
    @JsonProperty("severity_ranking")
    private String severityRanking;
    @JsonProperty("result")
    private CodeDescription result;
    @JsonProperty("disposition")
    private CodeDescription disposition;
    @JsonProperty("convicted")
    private boolean convicted;
    @JsonProperty("imprisonment_status")
    private CodeDescription imprisonmentStatus;
    @JsonProperty("band")
    private CodeDescription band;


}
