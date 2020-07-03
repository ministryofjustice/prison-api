package uk.gov.justice.hmpps.prison.repository.v1.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AlertSP {
    private String rootOffenderId;
    private String offenderBookId;
    private String activeFlag;
    private String agyLocId;
    private Integer alertSeq;
    private String alertType;
    private String alertTypeDesc;
    private String alertCode;
    private String alertCodeDesc;
    private LocalDate alertDate;
    private LocalDate expiryDate;
    private String alertStatus;
    private String commentText;
    private String authorizePersonText;
    private String caseloadId;
    private String verifiedFlag;

}
