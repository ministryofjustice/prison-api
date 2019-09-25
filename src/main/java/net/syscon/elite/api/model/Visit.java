package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Visit details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Visit details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Visit {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotBlank
    private String eventStatus;

    private String eventStatusDescription;

    @NotBlank
    private String visitType;

    private String visitTypeDescription;

    private String leadVisitor;

    private String relationship;

    private String relationshipDescription;

    @NotNull
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String location;

    @NotBlank
    private String eventOutcome;

    private String eventOutcomeDescription;

    private String cancellationReason;

    private String cancelReasonDescription;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * Status of event
     */
    @ApiModelProperty(required = true, value = "Status of event")
    @JsonProperty("eventStatus")
    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(final String eventStatus) {
        this.eventStatus = eventStatus;
    }

    /**
     * Description of eventStatus code
     */
    @ApiModelProperty(value = "Description of eventStatus code")
    @JsonProperty("eventStatusDescription")
    public String getEventStatusDescription() {
        return eventStatusDescription;
    }

    public void setEventStatusDescription(final String eventStatusDescription) {
        this.eventStatusDescription = eventStatusDescription;
    }

    /**
     * Social or official
     */
    @ApiModelProperty(required = true, value = "Social or official")
    @JsonProperty("visitType")
    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(final String visitType) {
        this.visitType = visitType;
    }

    /**
     * Description of visitType code
     */
    @ApiModelProperty(value = "Description of visitType code")
    @JsonProperty("visitTypeDescription")
    public String getVisitTypeDescription() {
        return visitTypeDescription;
    }

    public void setVisitTypeDescription(final String visitTypeDescription) {
        this.visitTypeDescription = visitTypeDescription;
    }

    /**
     * Name of main visitor
     */
    @ApiModelProperty(value = "Name of main visitor")
    @JsonProperty("leadVisitor")
    public String getLeadVisitor() {
        return leadVisitor;
    }

    public void setLeadVisitor(final String leadVisitor) {
        this.leadVisitor = leadVisitor;
    }

    /**
     * Relationship of main visitor to offender
     */
    @ApiModelProperty(value = "Relationship of main visitor to offender")
    @JsonProperty("relationship")
    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(final String relationship) {
        this.relationship = relationship;
    }

    /**
     * Description of relationship code
     */
    @ApiModelProperty(value = "Description of relationship code")
    @JsonProperty("relationshipDescription")
    public String getRelationshipDescription() {
        return relationshipDescription;
    }

    public void setRelationshipDescription(final String relationshipDescription) {
        this.relationshipDescription = relationshipDescription;
    }

    /**
     * Date and time at which event starts
     */
    @ApiModelProperty(required = true, value = "Date and time at which event starts")
    @JsonProperty("startTime")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(final LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Date and time at which event ends
     */
    @ApiModelProperty(value = "Date and time at which event ends")
    @JsonProperty("endTime")
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Location at which event takes place (could be an internal location, agency or external address).
     */
    @ApiModelProperty(value = "Location at which event takes place (could be an internal location, agency or external address).")
    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    /**
     * Whether attended or not
     */
    @ApiModelProperty(required = true, value = "Whether attended or not")
    @JsonProperty("eventOutcome")
    public String getEventOutcome() {
        return eventOutcome;
    }

    public void setEventOutcome(final String eventOutcome) {
        this.eventOutcome = eventOutcome;
    }

    /**
     * Description of eventOutcome code
     */
    @ApiModelProperty(value = "Description of eventOutcome code")
    @JsonProperty("eventOutcomeDescription")
    public String getEventOutcomeDescription() {
        return eventOutcomeDescription;
    }

    public void setEventOutcomeDescription(final String eventOutcomeDescription) {
        this.eventOutcomeDescription = eventOutcomeDescription;
    }

    /**
     * Reason if not attended
     */
    @ApiModelProperty(value = "Reason if not attended")
    @JsonProperty("cancellationReason")
    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(final String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    /**
     * Description of cancellationReason code
     */
    @ApiModelProperty(value = "Description of cancellationReason code")
    @JsonProperty("cancelReasonDescription")
    public String getCancelReasonDescription() {
        return cancelReasonDescription;
    }

    public void setCancelReasonDescription(final String cancelReasonDescription) {
        this.cancelReasonDescription = cancelReasonDescription;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class Visit {\n");

        sb.append("  eventStatus: ").append(eventStatus).append("\n");
        sb.append("  eventStatusDescription: ").append(eventStatusDescription).append("\n");
        sb.append("  visitType: ").append(visitType).append("\n");
        sb.append("  visitTypeDescription: ").append(visitTypeDescription).append("\n");
        sb.append("  leadVisitor: ").append(leadVisitor).append("\n");
        sb.append("  relationship: ").append(relationship).append("\n");
        sb.append("  relationshipDescription: ").append(relationshipDescription).append("\n");
        sb.append("  startTime: ").append(startTime).append("\n");
        sb.append("  endTime: ").append(endTime).append("\n");
        sb.append("  location: ").append(location).append("\n");
        sb.append("  eventOutcome: ").append(eventOutcome).append("\n");
        sb.append("  eventOutcomeDescription: ").append(eventOutcomeDescription).append("\n");
        sb.append("  cancellationReason: ").append(cancellationReason).append("\n");
        sb.append("  cancelReasonDescription: ").append(cancelReasonDescription).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
