package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Prisoner Schedule
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Prisoner Schedule")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PrisonerSchedule {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String offenderNo;

    private Long eventId;

    @NotNull
    private Long locationId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String cellLocation;

    @NotBlank
    private String event;

    @NotBlank
    private String eventType;

    @NotBlank
    private String eventDescription;

    @NotBlank
    private String eventStatus;

    @Length(max=4000) private String comment;

    @NotNull
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String eventOutcome;

    private String performance;

    private String outcomeComment;

    private Boolean paid;

    private BigDecimal payRate;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Offender number (e.g. NOMS Number).
      */
    @ApiModelProperty(required = true, value = "Offender number (e.g. NOMS Number).")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * Activity id if any. Used to attend or pay the event.
      */
    @ApiModelProperty(value = "Activity id if any. Used to attend or pay the event.")
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
      * The number which (uniquely) identifies the internal location associated with the Scheduled Event (Prisoner Schedule).
      */
    @ApiModelProperty(required = true, value = "The number which (uniquely) identifies the internal location associated with the Scheduled Event (Prisoner Schedule).")
    @JsonProperty("locationId")
    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    /**
      * Offender first name.
      */
    @ApiModelProperty(required = true, value = "Offender first name.")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
      * Offender last name.
      */
    @ApiModelProperty(required = true, value = "Offender last name.")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
      * Offender cell.
      */
    @ApiModelProperty(required = true, value = "Offender cell.")
    @JsonProperty("cellLocation")
    public String getCellLocation() {
        return cellLocation;
    }

    public void setCellLocation(String cellLocation) {
        this.cellLocation = cellLocation;
    }

    /**
      * Event code.
      */
    @ApiModelProperty(required = true, value = "Event code.")
    @JsonProperty("event")
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    /**
      * Event type, e.g. VISIT, APP, PRISON_ACT
      */
    @ApiModelProperty(required = true, value = "Event type, e.g. VISIT, APP, PRISON_ACT")
    @JsonProperty("eventType")
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
      * Description of event code.
      */
    @ApiModelProperty(required = true, value = "Description of event code.")
    @JsonProperty("eventDescription")
    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    /**
      * The event's status. Includes 'CANC', meaning cancelled for 'VISIT'
      */
    @ApiModelProperty(required = true, value = "The event's status. Includes 'CANC', meaning cancelled for 'VISIT'")
    @JsonProperty("eventStatus")
    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    /**
      * Details of event
      */
    @ApiModelProperty(value = "Details of event")
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
      * Date and time at which event starts
      */
    @ApiModelProperty(required = true, value = "Date and time at which event starts")
    @JsonProperty("startTime")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
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

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
      * Attendance, possible values are the codes in the 'PS_PA_OC' reference domain.
      */
    @ApiModelProperty(value = "Attendance, possible values are the codes in the 'PS_PA_OC' reference domain.")
    @JsonProperty("eventOutcome")
    public String getEventOutcome() {
        return eventOutcome;
    }

    public void setEventOutcome(String eventOutcome) {
        this.eventOutcome = eventOutcome;
    }

    /**
      * Possible values are the codes in the 'PERFORMANCE' reference domain.
      */
    @ApiModelProperty(value = "Possible values are the codes in the 'PERFORMANCE' reference domain.")
    @JsonProperty("performance")
    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    /**
      * No-pay reason
      */
    @ApiModelProperty(value = "No-pay reason")
    @JsonProperty("outcomeComment")
    public String getOutcomeComment() {
        return outcomeComment;
    }

    public void setOutcomeComment(String outcomeComment) {
        this.outcomeComment = outcomeComment;
    }

    /**
      * Activity paid flag.
      */
    @ApiModelProperty(value = "Activity paid flag.")
    @JsonProperty("paid")
    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    /**
      * Amount paid per activity session in pounds
      */
    @ApiModelProperty(value = "Amount paid per activity session in pounds")
    @JsonProperty("payRate")
    public BigDecimal getPayRate() {
        return payRate;
    }

    public void setPayRate(BigDecimal payRate) {
        this.payRate = payRate;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class PrisonerSchedule {\n");
        
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  eventId: ").append(eventId).append("\n");
        sb.append("  locationId: ").append(locationId).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  cellLocation: ").append(cellLocation).append("\n");
        sb.append("  event: ").append(event).append("\n");
        sb.append("  eventType: ").append(eventType).append("\n");
        sb.append("  eventDescription: ").append(eventDescription).append("\n");
        sb.append("  eventStatus: ").append(eventStatus).append("\n");
        sb.append("  comment: ").append(comment).append("\n");
        sb.append("  startTime: ").append(startTime).append("\n");
        sb.append("  endTime: ").append(endTime).append("\n");
        sb.append("  eventOutcome: ").append(eventOutcome).append("\n");
        sb.append("  performance: ").append(performance).append("\n");
        sb.append("  outcomeComment: ").append(outcomeComment).append("\n");
        sb.append("  paid: ").append(paid).append("\n");
        sb.append("  payRate: ").append(payRate).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
