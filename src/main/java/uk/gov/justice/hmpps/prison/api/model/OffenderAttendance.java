package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;


@Schema(description = "Information about an Offender's attendance at an activity")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderAttendance {
    @NotNull
    private Long bookingId;

    @NotNull
    @Schema(description = "The date of this activity")
    private LocalDate eventDate;

    @Schema(description = "Whether the offender attended", allowableValues = "ABS,ACCAB,ATT,CANC,NREQ,SUS,UNACAB,REST")
    private String outcome;

    @Schema(description = "The course code")
    private String code;

    @Schema(description = "The course description")
    private String description;

    @Schema(description = "The Prison ID", example = "MDI")
    private String prisonId;

    @Schema(description = "The current status for the offender on this activity")
    private String activityStatus;

    @Schema(description = "Activity name")
    private String activity;

    @Schema(description = "Attendance comment")
    private String comment;
}
