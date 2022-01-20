package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Visit details
 **/
@ApiModel(description = "Visit details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class VisitDetails {

    @JsonIgnore
    private Long id;

    @NotBlank
    @ApiModelProperty(required = true, value = "Status of event (EVENT_STS reference code)", allowableValues = "EXP,SCH,COMP,CANC")
    @JsonProperty("eventStatus")
    private String eventStatus;

    @ApiModelProperty(value = "Description of eventStatus code")
    @JsonProperty("eventStatusDescription")
    private String eventStatusDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Completion status of visit (VIS_COMPLETE reference code)", allowableValues = "NORM,SCH,VDE,OFFEND,VISITOR,CANC,HMPOP")
    @JsonProperty("completionStatus")
    private String completionStatus;

    @ApiModelProperty(value = "Description of completionStatus code")
    @JsonProperty("completionStatusDescription")
    private String completionStatusDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Code for social (SCON) or official (OFFI) type of visit (VISIT_TYPE reference code)", allowableValues = "OFFI,SCON")
    @JsonProperty("visitType")
    private String visitType;

    @ApiModelProperty(value = "Description of social or official visit", allowableValues = "Official Visit,Social Contact")
    @JsonProperty("visitTypeDescription")
    private String visitTypeDescription;

    @ApiModelProperty(value = "Name of lead visitor (blank if there was no visiting order for this visit)")
    @JsonProperty("leadVisitor")
    private String leadVisitor;

    @ApiModelProperty(value = "Relationship of lead visitor to offender")
    private String relationship;

    @ApiModelProperty(value = "Description of relationship code")
    @JsonProperty("relationshipDescription")
    private String relationshipDescription;

    @NotNull
    @ApiModelProperty(required = true, value = "Date and time at which event starts")
    @JsonProperty("startTime")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "Date and time at which event ends")
    @JsonProperty("endTime")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "Location at which event takes place (could be an internal location, agency or external address).")
    private String location;

    @ApiModelProperty(value = "Prison at which event takes place")
    private String prison;

    @NotBlank
    @ApiModelProperty(required = true, value = "Whether attended (ATT) or not (ABS) (OUTCOMES reference code)", allowableValues = "ATT,ABS")
    @JsonProperty("eventOutcome")
    private String eventOutcome;

    @ApiModelProperty(value = "Description of eventOutcome code")
    @JsonProperty("eventOutcomeDescription")
    private String eventOutcomeDescription;

    @ApiModelProperty(value = "Whether the visit was attended. Translation of eventOutcome into boolean. Defaults in NOMIS to true when the visit is created")
    @NotBlank
    private boolean attended;

    @ApiModelProperty(value = "Reason for cancellation if not attended (MOVE_CANC_RS reference code)")
    @JsonProperty("cancellationReason")
    private String cancellationReason;

    @ApiModelProperty(value = "Description of cancellationReason code")
    @JsonProperty("cancelReasonDescription")
    private String cancelReasonDescription;

    @ApiModelProperty(value = "List of visitors on visit")
    @JsonProperty("visitors")
    private List<Visitor> visitors;
}
