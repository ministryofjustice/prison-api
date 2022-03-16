package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Prisoner Schedule
 **/
@SuppressWarnings("unused")
@Schema(description = "Prisoner Schedule")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
@Data
public class PrisonerSchedule {
    @Schema(required = true, description = "Offender number (e.g. NOMS Number)")
    @NotBlank
    private String offenderNo;

    @Schema(description = "Activity id if any. Used to attend or pay the event")
    private Long eventId;

    @Schema(description = "Booking id for offender")
    private Long bookingId;

    @Schema(required = true, description = "The number which (uniquely) identifies the internal location associated with the Scheduled Event (Prisoner Schedule)")
    @NotNull
    private Long locationId;

    @Schema(required = true, description = "Offender first name")
    @NotBlank
    private String firstName;

    @Schema(required = true, description = "Offender last name")
    @NotBlank
    private String lastName;

    @Schema(required = true, description = "Offender cell")
    @NotBlank
    private String cellLocation;

    @Schema(required = true, description = "Event code")
    @NotBlank
    private String event;

    @Schema(required = true, description = "Event type, e.g. VISIT, APP, PRISON_ACT")
    @NotBlank
    private String eventType;

    @Schema(required = true, description = "Description of event code")
    @NotBlank
    private String eventDescription;

    @Schema(required = true, description = "Location of the event")
    private String eventLocation;

    @Schema(description = "Id of an internal event location")
    private Long eventLocationId;

    @Schema(required = true, description = "The event's status. Includes 'CANC', meaning cancelled for 'VISIT'")
    @NotBlank
    private String eventStatus;

    @Schema(required = true, description = "Comment")
    @Size(max = 4000)
    private String comment;

    @Schema(required = true, description = "Date and time at which event starts")
    @NotNull
    private LocalDateTime startTime;

    @Schema(description = "Date and time at which event ends")
    private LocalDateTime endTime;

    @Schema(description = "Attendance, possible values are the codes in the 'PS_PA_OC' reference domain")
    private String eventOutcome;

    @Schema(description = "Possible values are the codes in the 'PERFORMANCE' reference domain")
    private String performance;

    @Schema(description = "No-pay reason")
    private String outcomeComment;

    @Schema(description = "Activity paid flag")
    private Boolean paid;

    @Schema(description = "Amount paid per activity session in pounds")
    private BigDecimal payRate;

    @Schema(description = "Activity excluded flag")
    private Boolean excluded;

    @Schema(description = "Activity time slot")
    private TimeSlot timeSlot;

    @Schema(description = "The code for the activity location")
    private String locationCode;

    @Schema(description = "Event scheduled has been suspended")
    private Boolean suspended;

    public PrisonerSchedule(@NotBlank String offenderNo, Long eventId, Long bookingId, @NotNull Long locationId, @NotBlank String firstName, @NotBlank String lastName, @NotBlank String cellLocation, @NotBlank String event, @NotBlank String eventType, @NotBlank String eventDescription, String eventLocation, Long eventLocationId, @NotBlank String eventStatus, @Size(max = 4000) String comment, @NotNull LocalDateTime startTime, LocalDateTime endTime, String eventOutcome, String performance, String outcomeComment, Boolean paid, BigDecimal payRate, Boolean excluded, TimeSlot timeSlot, String locationCode, Boolean suspended) {
        this.offenderNo = offenderNo;
        this.eventId = eventId;
        this.bookingId = bookingId;
        this.locationId = locationId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cellLocation = cellLocation;
        this.event = event;
        this.eventType = eventType;
        this.eventDescription = eventDescription;
        this.eventLocation = eventLocation;
        this.eventLocationId = eventLocationId;
        this.eventStatus = eventStatus;
        this.comment = comment;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventOutcome = eventOutcome;
        this.performance = performance;
        this.outcomeComment = outcomeComment;
        this.paid = paid;
        this.payRate = payRate;
        this.excluded = excluded;
        this.timeSlot = timeSlot;
        this.locationCode = locationCode;
        this.suspended = suspended;
    }

    public PrisonerSchedule() {
    }
}
