package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Schema(description = "Prisoner Booking Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrisonerBookingSummary implements Serializable {

    @NotNull
    @Schema(required = true, description = "Unique, numeric booking id.", example = "1234134")
    private Long bookingId;

    @Schema(description = "Book number.", example = "A12121")
    private String bookingNo;

    @NotBlank
    @Schema(required = true, description = "Prisoner number (e.g. NOMS Number).", example = "A1234AA")
    private String offenderNo;

    @NotBlank
    @Schema(required = true, description = "Prisoner first name.", example = "JOHN")
    private String firstName;

    @Schema(description = "Prisoner's middle name.", example = "ASHLEY")
    private String middleName;

    @NotBlank
    @Schema(required = true, description = "Prisoner's last name.", example = "SMITH")
    private String lastName;

    @NotNull
    @Schema(required = true, description = "Prisoner's date of birth.", example = "1980-05-02")
    private LocalDate dateOfBirth;

    @NotNull
    @Schema(required = true, description = "Prisoner's current age.", example = "32")
    private Integer age;

    @NotBlank
    @Schema(required = true, description = "Identifier of agency that prisoner is associated with.", example = "MDI")
    private String agencyId;

    @Schema(description = "Identifier of living unit (e.g. cell) that prisoner is assigned to.", example = "123123")
    private Long assignedLivingUnitId;

    @Schema(description = "Identifier of facial image of prisoner.", example = "1241241")
    private Long facialImageId;

    @Schema(description = "The imprisonment status of the prisoner", example = "SENT")
    private String imprisonmentStatus;

    @Schema(description = "Legal Status", name = "legalStatus", example = "REMAND")
    private LegalStatus legalStatus;

    @Schema(description = "Convicted Status", name = "convictedStatus", example = "Convicted", allowableValues = {"Convicted","Remand"})
    private String convictedStatus;

    @Schema(description = "Description of living unit (e.g. cell) that prisoner is assigned to.", example = "MDI-1-1-3")
    private String assignedLivingUnitDesc;
}
