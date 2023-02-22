package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Scheduled Event
 **/
@SuppressWarnings("unused")
@Schema(description = "Scheduled Event")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class ScheduledEvent {
    @Schema(required = true, description = "Offender booking id")
    @NotNull
    private Long bookingId;

    @Schema(required = true, description = "Class of event")
    @NotBlank
    private String eventClass;

    @Schema(description = "Activity id if any. Used to attend or pay an activity.")
    private Long eventId;

    @Schema(required = true, description = "Status of event")
    @NotBlank
    private String eventStatus;

    @Schema(required = true, description = "Type of scheduled event (as a code)")
    @NotBlank
    private String eventType;

    @Schema(required = true, description = "Description of scheduled event type")
    @NotBlank
    private String eventTypeDesc;

    @Schema(required = true, description = "Sub type (or reason) of scheduled event (as a code)")
    @NotBlank
    private String eventSubType;

    @Schema(required = true, description = "Description of scheduled event sub type")
    @NotBlank
    private String eventSubTypeDesc;

    @Schema(required = true, description = "Date on which event occurs")
    @NotNull
    private LocalDate eventDate;

    @Schema(description = "Date and time at which event starts")
    private LocalDateTime startTime;

    @Schema(description = "Date and time at which event ends")
    private LocalDateTime endTime;

    @Schema(description = "Location at which event takes place (could be an internal location, agency or external address).")
    private String eventLocation;

    @Schema(description = "Id of an internal event location")
    private Long eventLocationId;

    @Schema(description = "The agency ID for the booked internal location", example = "WWI")
    private String agencyId;

    @Schema(required = true, description = "Code identifying underlying source of event data")
    @NotBlank
    private String eventSource;

    @Schema(description = "Source-specific code for the type or nature of the event")
    private String eventSourceCode;

    @Schema(description = "Source-specific description for type or nature of the event")
    private String eventSourceDesc;

    @Schema(description = "Activity attendance, possible values are the codes in the 'PS_PA_OC' reference domain.")
    private String eventOutcome;

    @Schema(description = "Activity performance, possible values are the codes in the 'PERFORMANCE' reference domain.")
    private String performance;

    @Schema(description = "Activity no-pay reason.")
    private String outcomeComment;

    @Schema(description = "Activity paid flag.")
    private Boolean paid;

    @Schema(description = "Amount paid per activity session in pounds")
    private BigDecimal payRate;

    @Schema(description = "The code for the activity location")
    private String locationCode;

    @Schema(description = "Staff member who created the appointment")
    private String createUserId;

    public ScheduledEvent(@NotNull Long bookingId, @NotBlank String eventClass, Long eventId, @NotBlank String eventStatus, @NotBlank String eventType, @NotBlank String eventTypeDesc, @NotBlank String eventSubType, @NotBlank String eventSubTypeDesc, @NotNull LocalDate eventDate, LocalDateTime startTime, LocalDateTime endTime, String eventLocation, Long eventLocationId, String agencyId, @NotBlank String eventSource, String eventSourceCode, String eventSourceDesc, String eventOutcome, String performance, String outcomeComment, Boolean paid, BigDecimal payRate, String locationCode, String createUserId) {
        this.bookingId = bookingId;
        this.eventClass = eventClass;
        this.eventId = eventId;
        this.eventStatus = eventStatus;
        this.eventType = eventType;
        this.eventTypeDesc = eventTypeDesc;
        this.eventSubType = eventSubType;
        this.eventSubTypeDesc = eventSubTypeDesc;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventLocation = eventLocation;
        this.eventLocationId = eventLocationId;
        this.agencyId = agencyId;
        this.eventSource = eventSource;
        this.eventSourceCode = eventSourceCode;
        this.eventSourceDesc = eventSourceDesc;
        this.eventOutcome = eventOutcome;
        this.performance = performance;
        this.outcomeComment = outcomeComment;
        this.paid = paid;
        this.payRate = payRate;
        this.locationCode = locationCode;
        this.createUserId = createUserId;
    }

    public ScheduledEvent() {
    }
}
