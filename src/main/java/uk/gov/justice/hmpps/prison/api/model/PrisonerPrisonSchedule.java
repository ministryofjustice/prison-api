package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Prisoner Prison Schedule
 **/
@SuppressWarnings("unused")
@Schema(description = "Prisoner Prison Schedule")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
@Data
public class PrisonerPrisonSchedule {
    @Schema(requiredMode = REQUIRED, description = "Offender number (e.g. NOMS Number)")
    @NotBlank
    private String offenderNo;

    @Schema(requiredMode = REQUIRED, description = "Offender first name")
    @NotBlank
    private String firstName;

    @Schema(requiredMode = REQUIRED, description = "Offender last name")
    @NotBlank
    private String lastName;

    @Schema(requiredMode = REQUIRED, description = "Event code")
    @NotBlank
    private String event;

    @Schema(requiredMode = REQUIRED, description = "Event type, e.g. VISIT, APP, PRISON_ACT")
    @NotBlank
    private String eventType;

    @Schema(requiredMode = REQUIRED, description = "Description of event code")
    @NotBlank
    private String eventDescription;

    @Schema(requiredMode = REQUIRED, description = "Location of the event")
    private String eventLocation;

    @Schema(requiredMode = REQUIRED, description = "The event's status. Includes 'CANC', meaning cancelled for 'VISIT'")
    @NotBlank
    private String eventStatus;

    @Schema(requiredMode = REQUIRED, description = "Comment")
    @Size(max = 4000)
    private String comment;

    @Schema(requiredMode = REQUIRED, description = "Date and time at which event starts")
    @NotNull
    private LocalDateTime startTime;

    @Schema(description = "Date and time at which event ends")
    private LocalDateTime endTime;

    public PrisonerPrisonSchedule(@NotBlank String offenderNo, @NotBlank String firstName, @NotBlank String lastName, @NotBlank String event, @NotBlank String eventType, @NotBlank String eventDescription, String eventLocation, @NotBlank String eventStatus, @Size(max = 4000) String comment, @NotNull LocalDateTime startTime, LocalDateTime endTime) {
        this.offenderNo = offenderNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.event = event;
        this.eventType = eventType;
        this.eventDescription = eventDescription;
        this.eventLocation = eventLocation;
        this.eventStatus = eventStatus;
        this.comment = comment;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public PrisonerPrisonSchedule() {
    }
}
