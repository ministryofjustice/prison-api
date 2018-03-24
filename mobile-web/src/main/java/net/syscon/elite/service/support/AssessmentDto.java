package net.syscon.elite.service.support;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
@EqualsAndHashCode(of = { "assessmentCode", "assessmentSeq", "assessmentDate", "bookingId", "offenderNo"})
public class AssessmentDto {
    private Long bookingId;
    private String offenderNo;
    private String assessmentCode;
    private String assessmentDescription;
    private LocalDate assessmentDate;
    private int assessmentSeq;

    private LocalDate nextReviewDate;

    private String reviewSupLevelType;
    private String reviewSupLevelTypeDesc;
    private String overridedSupLevelType;
    private String overridedSupLevelTypeDesc;
    private String calcSupLevelType;
    private String calcSupLevelTypeDesc;

    private boolean cellSharingAlertFlag;
    private String assessStatus;
    private String caseloadType;
}
