package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "Prisoner Schedule")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class PrisonerSchedule {
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Map<String, Object> additionalProperties;

    @ApiModelProperty(required = true, value = "Offender number (e.g. NOMS Number)")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(value = "Activity id if any. Used to attend or pay the event")
    private Long eventId;

    @ApiModelProperty(value = "Booking id for offender")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "The number which (uniquely) identifies the internal location associated with the Scheduled Event (Prisoner Schedule)")
    @NotNull
    private Long locationId;

    @ApiModelProperty(required = true, value = "Offender first name")
    @NotBlank
    private String firstName;

    @ApiModelProperty(required = true, value = "Offender last name")
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "Offender cell")
    @NotBlank
    private String cellLocation;

    @ApiModelProperty(required = true, value = "Event code")
    @NotBlank
    private String event;

    @ApiModelProperty(required = true, value = "Event type, e.g. VISIT, APP, PRISON_ACT")
    @NotBlank
    private String eventType;

    @ApiModelProperty(required = true, value = "Description of event code")
    @NotBlank
    private String eventDescription;

    @ApiModelProperty(required = true, value = "Location of the event")
    private String eventLocation;

    @ApiModelProperty(value = "Id of an internal event location")
    private Long eventLocationId;

    @ApiModelProperty(required = true, value = "The event's status. Includes 'CANC', meaning cancelled for 'VISIT'")
    @NotBlank
    private String eventStatus;

    @ApiModelProperty(required = true, value = "Comment")
    @Size(max = 4000)
    private String comment;

    @ApiModelProperty(required = true, value = "Date and time at which event starts")
    @NotNull
    private LocalDateTime startTime;

    @ApiModelProperty(value = "Date and time at which event ends")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "Attendance, possible values are the codes in the 'PS_PA_OC' reference domain")
    private String eventOutcome;

    @ApiModelProperty(value = "Possible values are the codes in the 'PERFORMANCE' reference domain")
    private String performance;

    @ApiModelProperty(value = "No-pay reason")
    private String outcomeComment;

    @ApiModelProperty(value = "Activity paid flag")
    private Boolean paid;

    @ApiModelProperty(value = "Amount paid per activity session in pounds")
    private BigDecimal payRate;

    @ApiModelProperty(value = "Activity excluded flag")
    private Boolean excluded;

    @ApiModelProperty(value = "Activity time slot")
    private TimeSlot timeSlot;

    @ApiModelProperty(value = "The code for the activity location")
    private String locationCode;

    @ApiModelProperty(value = "Event scheduled has been suspended")
    private Boolean suspended;
}
