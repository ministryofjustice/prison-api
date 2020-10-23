package uk.gov.justice.hmpps.prison.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"incidentCaseId", "questionnaireQueId"})
@Data
class FlatIncidentCase {

    private Long incidentCaseId;
    private String incidentTitle;
    private String incidentType;
    private String incidentDetails;
    private LocalDate incidentDate;
    private LocalDateTime incidentTime;
    private Long reportedStaffId;
    private LocalDate reportDate;
    private LocalDateTime reportTime;
    private String incidentStatus;
    private String agencyId;
    private Boolean responseLockedFlag;

    private String question;
    private String answer;
    private int questionSeq;
    private Long questionnaireQueId;
    private Long questionnaireAnsId;
    private LocalDateTime responseDate;
    private String responseCommentText;
    private Long recordStaffId;

}
