package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@SuppressWarnings("unused")
@Schema(description = "Summary data for a completed movement")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OutOnTemporaryAbsenceSummary {

    @Schema(requiredMode = REQUIRED, description = "Offender number (NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Prisoner first name.", example = "JOHN")
    private String firstName;

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Prisoner's last name.", example = "SMITH")
    private String lastName;

    @NotNull
    @Schema(requiredMode = REQUIRED, description = "Prisoner's date of birth.", example = "1980-05-02")
    private LocalDate dateOfBirth;

    @Schema(requiredMode = REQUIRED, description = "Movement date and time in Europe/London local time format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime movementTime;

    @Schema(description = "Agency travelling to", example = "MDI")
    private String toAgency;

    @Schema(description = "Description of the agency travelling to", example = "HMP MOORLANDS")
    private String toAgencyDescription;

    @Schema(description = "City offender was sent to", example = "DONCASTER")
    private String toCity;

    @Schema(requiredMode = REQUIRED, description = "The movement reason code", example = "C1")
    private String movementReasonCode;

    @Schema(requiredMode = REQUIRED, description = "Description of movement reason", example = "Convicted at court")
    private String movementReason;

    @Schema(description = "Comment", example = "This is a free text comment")
    private String commentText;
}
