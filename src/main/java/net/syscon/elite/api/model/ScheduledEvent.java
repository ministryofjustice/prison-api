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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Scheduled Event
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Scheduled Event")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ScheduledEvent {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    @NotBlank
    private String eventClass;

    private Long eventId;

    @NotBlank
    private String eventStatus;

    @NotBlank
    private String eventType;

    @NotBlank
    private String eventTypeDesc;

    @NotBlank
    private String eventSubType;

    @NotBlank
    private String eventSubTypeDesc;

    @NotNull
    private LocalDate eventDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String eventLocation;

    @NotBlank
    private String eventSource;

    private String eventSourceCode;

    private String eventSourceDesc;

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
      * Offender booking id
      */
    @ApiModelProperty(required = true, value = "Offender booking id")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Class of event
      */
    @ApiModelProperty(required = true, value = "Class of event")
    @JsonProperty("eventClass")
    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }

    /**
      * Activity id if any. Used to attend or pay an activity.
      */
    @ApiModelProperty(value = "Activity id if any. Used to attend or pay an activity.")
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
      * Status of event
      */
    @ApiModelProperty(required = true, value = "Status of event")
    @JsonProperty("eventStatus")
    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    /**
      * Type of scheduled event (as a code)
      */
    @ApiModelProperty(required = true, value = "Type of scheduled event (as a code)")
    @JsonProperty("eventType")
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
      * Description of scheduled event type
      */
    @ApiModelProperty(required = true, value = "Description of scheduled event type")
    @JsonProperty("eventTypeDesc")
    public String getEventTypeDesc() {
        return eventTypeDesc;
    }

    public void setEventTypeDesc(String eventTypeDesc) {
        this.eventTypeDesc = eventTypeDesc;
    }

    /**
      * Sub type (or reason) of scheduled event (as a code)
      */
    @ApiModelProperty(required = true, value = "Sub type (or reason) of scheduled event (as a code)")
    @JsonProperty("eventSubType")
    public String getEventSubType() {
        return eventSubType;
    }

    public void setEventSubType(String eventSubType) {
        this.eventSubType = eventSubType;
    }

    /**
      * Description of scheduled event sub type
      */
    @ApiModelProperty(required = true, value = "Description of scheduled event sub type")
    @JsonProperty("eventSubTypeDesc")
    public String getEventSubTypeDesc() {
        return eventSubTypeDesc;
    }

    public void setEventSubTypeDesc(String eventSubTypeDesc) {
        this.eventSubTypeDesc = eventSubTypeDesc;
    }

    /**
      * Date on which event occurs
      */
    @ApiModelProperty(required = true, value = "Date on which event occurs")
    @JsonProperty("eventDate")
    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    /**
      * Date and time at which event starts
      */
    @ApiModelProperty(value = "Date and time at which event starts")
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
      * Location at which event takes place (could be an internal location, agency or external address).
      */
    @ApiModelProperty(value = "Location at which event takes place (could be an internal location, agency or external address).")
    @JsonProperty("eventLocation")
    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    /**
      * Code identifying underlying source of event data
      */
    @ApiModelProperty(required = true, value = "Code identifying underlying source of event data")
    @JsonProperty("eventSource")
    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    /**
      * Source-specific code for the type or nature of the event
      */
    @ApiModelProperty(value = "Source-specific code for the type or nature of the event")
    @JsonProperty("eventSourceCode")
    public String getEventSourceCode() {
        return eventSourceCode;
    }

    public void setEventSourceCode(String eventSourceCode) {
        this.eventSourceCode = eventSourceCode;
    }

    /**
      * Source-specific description for type or nature of the event
      */
    @ApiModelProperty(value = "Source-specific description for type or nature of the event")
    @JsonProperty("eventSourceDesc")
    public String getEventSourceDesc() {
        return eventSourceDesc;
    }

    public void setEventSourceDesc(String eventSourceDesc) {
        this.eventSourceDesc = eventSourceDesc;
    }

    /**
      * Activity attendance, possible values are the codes in the 'PS_PA_OC' reference domain.
      */
    @ApiModelProperty(value = "Activity attendance, possible values are the codes in the 'PS_PA_OC' reference domain.")
    @JsonProperty("eventOutcome")
    public String getEventOutcome() {
        return eventOutcome;
    }

    public void setEventOutcome(String eventOutcome) {
        this.eventOutcome = eventOutcome;
    }

    /**
      * Activity performance, possible values are the codes in the 'PERFORMANCE' reference domain.
      */
    @ApiModelProperty(value = "Activity performance, possible values are the codes in the 'PERFORMANCE' reference domain.")
    @JsonProperty("performance")
    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    /**
      * Activity no-pay reason.
      */
    @ApiModelProperty(value = "Activity no-pay reason.")
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

        sb.append("class ScheduledEvent {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  eventClass: ").append(eventClass).append("\n");
        sb.append("  eventId: ").append(eventId).append("\n");
        sb.append("  eventStatus: ").append(eventStatus).append("\n");
        sb.append("  eventType: ").append(eventType).append("\n");
        sb.append("  eventTypeDesc: ").append(eventTypeDesc).append("\n");
        sb.append("  eventSubType: ").append(eventSubType).append("\n");
        sb.append("  eventSubTypeDesc: ").append(eventSubTypeDesc).append("\n");
        sb.append("  eventDate: ").append(eventDate).append("\n");
        sb.append("  startTime: ").append(startTime).append("\n");
        sb.append("  endTime: ").append(endTime).append("\n");
        sb.append("  eventLocation: ").append(eventLocation).append("\n");
        sb.append("  eventSource: ").append(eventSource).append("\n");
        sb.append("  eventSourceCode: ").append(eventSourceCode).append("\n");
        sb.append("  eventSourceDesc: ").append(eventSourceDesc).append("\n");
        sb.append("  eventOutcome: ").append(eventOutcome).append("\n");
        sb.append("  performance: ").append(performance).append("\n");
        sb.append("  outcomeComment: ").append(outcomeComment).append("\n");
        sb.append("  paid: ").append(paid).append("\n");
        sb.append("  payRate: ").append(payRate).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
