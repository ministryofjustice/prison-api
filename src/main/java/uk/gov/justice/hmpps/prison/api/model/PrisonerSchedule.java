package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Prisoner Schedule
 **/
@SuppressWarnings("unused")
@Schema(description = "Prisoner Schedule")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class PrisonerSchedule {
    @JsonIgnore
    @Hidden
    private Map<String, Object> additionalProperties;

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
}
