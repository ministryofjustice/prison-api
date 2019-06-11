package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@ApiModel(description = "A scheduled offender movement event")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferEvent {

    @NotBlank
    @ApiModelProperty(required = true, value = "Offender number(NOMS ID)")
    private String offenderNo;

    @NotBlank
    @ApiModelProperty(required = true, value = "Date and time the record was created")
    private LocalDateTime createDateTime;

    @ApiModelProperty(required = true, value = "The internal event ID")
    private Long eventId;

    @ApiModelProperty(required = true, value = "The agency code from which the event will start")
    private String fromAgency;

    @ApiModelProperty(required = true, value = "The from agency description")
    private String fromAgencyDescription;

    @ApiModelProperty(required = true, value = "The agency code to which the transfer will be made (if an agency)")
    private String toAgency;

    @ApiModelProperty(required = true, value = "The to agency description")
    private String toAgencyDescription;

    @ApiModelProperty(required = true, value = "The destination city when available")
    private String toCity;

    @ApiModelProperty(required = true, value = "The event status - either SCH or COMP")
    private String eventStatus;

    @ApiModelProperty(required = true, value = "The event class - from OFFENDER_IND_SCHEDULES")
    private String eventClass;

    @ApiModelProperty(required = true, value = "The event type - from OFFENDER_IND_SCHEDULES")
    private String eventType;

    @ApiModelProperty(required = true, value = "The event sub-type - from OFFENDER_IND_SCHEDULES")
    private String eventSubType;

    @ApiModelProperty(required = true, value = "The date on which the event is scheduled to occur")
    private LocalDate eventDate;

    @ApiModelProperty(required = true, value = "The planned date and time of the start of the event")
    private LocalDateTime startTime;

    @ApiModelProperty(required = true, value = "The planned date and time of the end of the event")
    private LocalDateTime endTime;

    @ApiModelProperty(required = true, value = "The outcome reason code - from offender_ind_schedules")
    private String outcomeReasonCode;

    @ApiModelProperty(required = true, value = "The name of the judge where known")
    private String judgeName;

    @ApiModelProperty(required = true, value = "Engagement code")
    private String engagementCode;

    @ApiModelProperty(required = true, value = "The escort code")
    private String escortCode;

    @ApiModelProperty(required = true, value = "The performance code")
    private String performanceCode;

    @ApiModelProperty(required = true, value = "The direction code (IN or OUT)")
    private String directionCode;

    @ApiModelProperty(required = true, value = "The booking active flag - either Y or N from offender bookings")
    private String bookingActiveFlag;

    @ApiModelProperty(required = true, value = "The booking in or out status - either IN or OUR from offender bookings")
    private String bookingInOutStatus;
}
