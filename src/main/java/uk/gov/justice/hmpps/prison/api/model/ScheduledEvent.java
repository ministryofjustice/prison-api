package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Scheduled Event
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Scheduled Event")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScheduledEvent {
    @ApiModelProperty(required = true, value = "Offender booking id")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Class of event")
    @NotBlank
    private String eventClass;

    @ApiModelProperty(value = "Activity id if any. Used to attend or pay an activity.")
    private Long eventId;

    @ApiModelProperty(required = true, value = "Status of event")
    @NotBlank
    private String eventStatus;

    @ApiModelProperty(required = true, value = "Type of scheduled event (as a code)")
    @NotBlank
    private String eventType;

    @ApiModelProperty(required = true, value = "Description of scheduled event type")
    @NotBlank
    private String eventTypeDesc;

    @ApiModelProperty(required = true, value = "Sub type (or reason) of scheduled event (as a code)")
    @NotBlank
    private String eventSubType;

    @ApiModelProperty(required = true, value = "Description of scheduled event sub type")
    @NotBlank
    private String eventSubTypeDesc;

    @ApiModelProperty(required = true, value = "Date on which event occurs")
    @NotNull
    private LocalDate eventDate;

    @ApiModelProperty(value = "Date and time at which event starts")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "Date and time at which event ends")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "Location at which event takes place (could be an internal location, agency or external address).")
    private String eventLocation;

    @ApiModelProperty(value = "Id of an internal event location")
    private Long eventLocationId;

    @JsonIgnore
    private String agencyId;

    @ApiModelProperty(required = true, value = "Code identifying underlying source of event data")
    @NotBlank
    private String eventSource;

    @ApiModelProperty(value = "Source-specific code for the type or nature of the event")
    private String eventSourceCode;

    @ApiModelProperty(value = "Source-specific description for type or nature of the event")
    private String eventSourceDesc;

    @ApiModelProperty(value = "Activity attendance, possible values are the codes in the 'PS_PA_OC' reference domain.")
    private String eventOutcome;

    @ApiModelProperty(value = "Activity performance, possible values are the codes in the 'PERFORMANCE' reference domain.")
    private String performance;

    @ApiModelProperty(value = "Activity no-pay reason.")
    private String outcomeComment;

    @ApiModelProperty(value = "Activity paid flag.")
    private Boolean paid;

    @ApiModelProperty(value = "Amount paid per activity session in pounds")
    private BigDecimal payRate;

    @ApiModelProperty(value = "The code for the activity location")
    private String locationCode;
}
