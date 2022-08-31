package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Offender Event")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime eventDatetime;

    private Long scheduleEventId;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private String scheduleEventClass;
    private String scheduleEventType;
    private String scheduleEventSubType;
    private String scheduleEventStatus;
    private Boolean recordDeleted;
    private String recordDeletedString;
    private String deleteFlag;

    private Long rootOffenderId;
    private Long offenderId;
    private Long aliasOffenderId;
    private Long previousOffenderId;
    private String offenderIdDisplay;

    private Long bookingId;
    private String bookingNumber;
    private String previousBookingNumber;

    private Long sanctionSeq;
    private Long movementSeq;
    private Long imprisonmentStatusSeq;
    private Long assessmentSeq;

    private Long alertSeq;
    private LocalDateTime alertDateTime;
    private String alertType;
    private String alertCode;
    private LocalDateTime expiryDateTime;

    private Long caseNoteId;
    private String agencyLocationId;
    private Long riskPredictorId;
    private Long addressId;
    private Long personId;
    private Long sentenceCalculationId;
    private Long oicHearingId;
    private Long oicOffenceId;
    private String pleaFindingCode;
    private String findingCode;

    private Long resultSeq;
    private Long agencyIncidentId;
    private Long chargeSeq;

    private String identifierType;
    private String identifierValue;

    private Long ownerId;
    private String ownerClass;

    private Long sentenceSeq;
    private String conditionCode;
    private Long offenderSentenceConditionId;

    private LocalDate addressEndDate;
    private String primaryAddressFlag;
    private String mailAddressFlag;
    private String addressUsage;

    // incident event data
    private Long incidentCaseId;
    private Long incidentPartySeq;
    private Long incidentRequirementSeq;
    private Long incidentQuestionSeq;
    private Long incidentResponseSeq;

    // bed assignment data
    private Integer bedAssignmentSeq;
    private Long livingUnitId;

    // external movement event data
    private LocalDateTime movementDateTime;
    private String movementType;
    private String movementReasonCode;
    private String directionCode;
    private String escortCode;
    private String fromAgencyLocationId;
    private String toAgencyLocationId;

    // iep data
    private Long iepSeq;
    private String iepLevel;

    private String nomisEventType;
    private String auditModuleName;
}
