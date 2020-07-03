package net.syscon.prison.repository.v1.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LegalCaseSP {

    private Long caseId;
    private String caseInfoNumber;
    private LocalDate beginDate;
    private String caseStatus;
    private String courtCode;
    private String courtDesc;
    private String caseTypeCode;
    private String caseTypeDesc;

}
