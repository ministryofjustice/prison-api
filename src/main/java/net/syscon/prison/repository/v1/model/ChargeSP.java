package net.syscon.prison.repository.v1.model;

import lombok.Data;

@Data
public class ChargeSP {

    private Long offenderChargeId;
    private String statuteCode;
    private String offenceCode;
    private Integer noOfOffences;
    private String mostSeriousFlag;
    private String chargeStatus;
    private String severityRanking;
    private String offenceDesc;
    private String statuteDesc;
    private String resultCode;
    private String resultDesc;
    private String dispositionCode;
    private String dispositionDesc;
    private String convictionFlag;
    private String imprisonmentStatus;
    private String imprisonmentStatusDesc;
    private String bandCode;
    private String bandDesc;

}
