package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Visit details
 **/
@Schema(description = "Visit details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode
public class VisitDetails {

    @JsonIgnore
    private Long id;

    @NotBlank
    @Schema(required = true, description = "Status of event (EVENT_STS reference code)", allowableValues = {"EXP","SCH","COMP","CANC"})
    @JsonProperty("eventStatus")
    private String eventStatus;

    @Schema(description = "Description of eventStatus code")
    @JsonProperty("eventStatusDescription")
    private String eventStatusDescription;

    @NotBlank
    @Schema(required = true, description = "Completion status of visit (VIS_COMPLETE reference code)", allowableValues = {"NORM","SCH","VDE","OFFEND","VISITOR","CANC","HMPOP"})
    @JsonProperty("completionStatus")
    private String completionStatus;

    @Schema(description = "Description of completionStatus code")
    @JsonProperty("completionStatusDescription")
    private String completionStatusDescription;

    @NotBlank
    @Schema(required = true, description = "Code for social (SCON) or official (OFFI) type of visit (VISIT_TYPE reference code)", allowableValues = {"OFFI","SCON"})
    @JsonProperty("visitType")
    private String visitType;

    @Schema(description = "Description of social or official visit", allowableValues = {"Official Visit","Social Contact"})
    @JsonProperty("visitTypeDescription")
    private String visitTypeDescription;

    @Schema(description = "Name of lead visitor (blank if there was no visiting order for this visit)")
    @JsonProperty("leadVisitor")
    private String leadVisitor;

    @Schema(description = "Relationship of lead visitor to offender")
    private String relationship;

    @Schema(description = "Description of relationship code")
    @JsonProperty("relationshipDescription")
    private String relationshipDescription;

    @NotNull
    @Schema(required = true, description = "Date and time at which event starts")
    @JsonProperty("startTime")
    private LocalDateTime startTime;

    @Schema(description = "Date and time at which event ends")
    @JsonProperty("endTime")
    private LocalDateTime endTime;

    @Schema(description = "Location at which event takes place (could be an internal location, agency or external address).")
    private String location;

    @Schema(description = "Prison at which event takes place")
    private String prison;

    @NotBlank
    @Schema(required = true, description = "Whether attended (ATT) or not (ABS) (OUTCOMES reference code)", allowableValues = {"ATT","ABS"})
    @JsonProperty("eventOutcome")
    private String eventOutcome;

    @Schema(description = "Description of eventOutcome code")
    @JsonProperty("eventOutcomeDescription")
    private String eventOutcomeDescription;

    @Schema(description = "Whether the visit was attended. Translation of eventOutcome into boolean. Defaults in NOMIS to true when the visit is created")
    @NotBlank
    private boolean attended;

    @Schema(description = "Reason for cancellation if not attended (MOVE_CANC_RS reference code)")
    @JsonProperty("cancellationReason")
    private String cancellationReason;

    @Schema(description = "Description of cancellationReason code")
    @JsonProperty("cancelReasonDescription")
    private String cancelReasonDescription;

    @Schema(description = "List of visitors on visit")
    @JsonProperty("visitors")
    private List<Visitor> visitors;

    @Schema(description = "Type of search performed - mandatory if visit completed (SEARCH_LEVEL reference code)", example = "FULL")
    @JsonProperty("searchType")
    private String searchType;

    @Schema(description = "Description of searchType code")
    @JsonProperty("searchTypeDescription")
    private String searchTypeDescription;

    public VisitDetails(Long id, @NotBlank String eventStatus, String eventStatusDescription, @NotBlank String completionStatus, String completionStatusDescription, @NotBlank String visitType, String visitTypeDescription, String leadVisitor, String relationship, String relationshipDescription, @NotNull LocalDateTime startTime, LocalDateTime endTime, String location, String prison, @NotBlank String eventOutcome, String eventOutcomeDescription, @NotBlank boolean attended, String cancellationReason, String cancelReasonDescription, List<Visitor> visitors, String searchType, String searchTypeDescription) {
        this.id = id;
        this.eventStatus = eventStatus;
        this.eventStatusDescription = eventStatusDescription;
        this.completionStatus = completionStatus;
        this.completionStatusDescription = completionStatusDescription;
        this.visitType = visitType;
        this.visitTypeDescription = visitTypeDescription;
        this.leadVisitor = leadVisitor;
        this.relationship = relationship;
        this.relationshipDescription = relationshipDescription;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.prison = prison;
        this.eventOutcome = eventOutcome;
        this.eventOutcomeDescription = eventOutcomeDescription;
        this.attended = attended;
        this.cancellationReason = cancellationReason;
        this.cancelReasonDescription = cancelReasonDescription;
        this.visitors = visitors;
        this.searchType = searchType;
        this.searchTypeDescription = searchTypeDescription;
    }

    public VisitDetails() {
    }
}
