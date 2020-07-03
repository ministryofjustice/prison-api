package net.syscon.prison.api.model;

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

    @NotBlank
    @ApiModelProperty(required = true, value = "Status of event")
    @JsonProperty("eventStatus")
    private String eventStatus;

    @ApiModelProperty(value = "Description of eventStatus code")
    @JsonProperty("eventStatusDescription")
    private String eventStatusDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Social or official")
    @JsonProperty("visitType")
    private String visitType;

    @ApiModelProperty(value = "Description of visitType code")
    @JsonProperty("visitTypeDescription")
    private String visitTypeDescription;

    @ApiModelProperty(value = "Name of main visitor")
    @JsonProperty("leadVisitor")
    private String leadVisitor;

    @ApiModelProperty(value = "Relationship of main visitor to offender")
    @JsonProperty("relationship")
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
    @JsonProperty("location")
    private String location;

    @NotBlank
    @ApiModelProperty(required = true, value = "Whether attended or not")
    @JsonProperty("eventOutcome")
    private String eventOutcome;

    @ApiModelProperty(value = "Description of eventOutcome code")
    @JsonProperty("eventOutcomeDescription")
    private String eventOutcomeDescription;

    @ApiModelProperty(value = "Reason if not attended")
    @JsonProperty("cancellationReason")
    private String cancellationReason;

    @ApiModelProperty(value = "Description of cancellationReason code")
    @JsonProperty("cancelReasonDescription")
    private String cancelReasonDescription;
}
