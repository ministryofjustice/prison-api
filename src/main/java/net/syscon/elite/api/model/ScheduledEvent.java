package net.syscon.elite.api.model;

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
    /**
     * Offender booking id
     */
    @ApiModelProperty(required = true, value = "Offender booking id")
    @NotNull
    private Long bookingId;

    /**
     * Class of event
     */
    @ApiModelProperty(required = true, value = "Class of event")
    @NotBlank
    private String eventClass;

    /**
     * Activity id if any. Used to attend or pay an activity.
     */
    @ApiModelProperty(value = "Activity id if any. Used to attend or pay an activity.")
    private Long eventId;

    /**
     * Status of event
     */
    @ApiModelProperty(required = true, value = "Status of event")
    @NotBlank
    private String eventStatus;

    /**
     * Type of scheduled event (as a code)
     */
    @ApiModelProperty(required = true, value = "Type of scheduled event (as a code)")
    @NotBlank
    private String eventType;

    /**
     * Description of scheduled event type
     */
    @ApiModelProperty(required = true, value = "Description of scheduled event type")
    @NotBlank
    private String eventTypeDesc;

    /**
     * Sub type (or reason) of scheduled event (as a code)
     */
    @ApiModelProperty(required = true, value = "Sub type (or reason) of scheduled event (as a code)")
    @NotBlank
    private String eventSubType;

    /**
     * Description of scheduled event sub type
     */
    @ApiModelProperty(required = true, value = "Description of scheduled event sub type")
    @NotBlank
    private String eventSubTypeDesc;

    /**
     * Date on which event occurs
     */
    @ApiModelProperty(required = true, value = "Date on which event occurs")
    @NotNull
    private LocalDate eventDate;

    /**
     * Date and time at which event starts
     */
    @ApiModelProperty(value = "Date and time at which event starts")
    private LocalDateTime startTime;

    /**
     * Date and time at which event ends
     */
    @ApiModelProperty(value = "Date and time at which event ends")
    private LocalDateTime endTime;

    /**
     * Location at which event takes place (could be an internal location, agency or external address).
     */
    @ApiModelProperty(value = "Location at which event takes place (could be an internal location, agency or external address).")
    private String eventLocation;

    /**
     * Code identifying underlying source of event data
     */
    @ApiModelProperty(required = true, value = "Code identifying underlying source of event data")
    @NotBlank
    private String eventSource;

    /**
     * Source-specific code for the type or nature of the event
     */
    @ApiModelProperty(value = "Source-specific code for the type or nature of the event")
    private String eventSourceCode;

    /**
     * Source-specific description for type or nature of the event
     */
    @ApiModelProperty(value = "Source-specific description for type or nature of the event")
    private String eventSourceDesc;


    /**
     * Activity attendance, possible values are the codes in the 'PS_PA_OC' reference domain.
     */
    @ApiModelProperty(value = "Activity attendance, possible values are the codes in the 'PS_PA_OC' reference domain.")
    private String eventOutcome;

    /**
     * Activity performance, possible values are the codes in the 'PERFORMANCE' reference domain.
     */
    @ApiModelProperty(value = "Activity performance, possible values are the codes in the 'PERFORMANCE' reference domain.")
    private String performance;

    /**
     * Activity no-pay reason.
     */
    @ApiModelProperty(value = "Activity no-pay reason.")
    private String outcomeComment;

    /**
     * Activity paid flag.
     */
    @ApiModelProperty(value = "Activity paid flag.")
    private Boolean paid;

    /**
     * Amount paid per activity session in pounds
     */
    @ApiModelProperty(value = "Amount paid per activity session in pounds")
    private BigDecimal payRate;
}
