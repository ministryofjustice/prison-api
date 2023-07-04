package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Offender Booking Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderBooking implements CategoryCodeAware {

    @NotNull
    @Schema(requiredMode = REQUIRED, description = "Unique, numeric booking id.", example = "1234134")
    private Long bookingId;

    @Schema(description = "Booking number.", example = "A12121")
    private String bookingNo;

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Offender number (e.g. NOMS Number).", example = "A1234AA")
    private String offenderNo;

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Offender first name.", example = "JOHN")
    private String firstName;

    @Schema(description = "Offender middle name.", example = "ASHLEY")
    private String middleName;

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Offender last name.", example = "SMITH")
    private String lastName;

    @NotNull
    @Schema(requiredMode = REQUIRED, description = "Offender date of birth.", example = "1980-05-02")
    private LocalDate dateOfBirth;

    @NotNull
    @Schema(requiredMode = REQUIRED, description = "Offender's current age.", example = "32")
    private Integer age;

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Identifier of agency that offender is associated with.", example = "MDI")
    private String agencyId;

    @Schema(description = "Identifier of living unit (e.g. cell) that offender is assigned to.", example = "123123")
    private Long assignedLivingUnitId;

    @Schema(description = "Description of living unit (e.g. cell) that offender is assigned to.", example = "MDI-1-1-3")
    private String assignedLivingUnitDesc;

    @Schema(description = "Identifier of facial image of offender.", example = "1241241")
    private Long facialImageId;

    @Schema(description = "Identifier of officer (key worker) to which offender is assigned.", example = "354543")
    private String assignedOfficerUserId;

    @Schema(description = "List of offender's alias names.")
    private List<String> aliases;

    @Schema(description = "The Cat A/B/C/D of the offender", example = "C", allowableValues = {"A","B","C","D","I","J"})
    private String categoryCode;

    @Schema(description = "Convicted Status", name = "convictedStatus", example = "Convicted", allowableValues = {"Convicted","Remand"})
    private String convictedStatus;

    @JsonIgnore
    private String bandCode;

    @Schema(description = "The imprisonment status of the offender", example = "SENT")
    private String imprisonmentStatus;

    @NotNull
    @Builder.Default
    @Schema(requiredMode = REQUIRED, description = "List of offender's current alert types.")
    private List<String> alertsCodes = new ArrayList<>();

    @NotNull
    @Builder.Default
    @Schema(requiredMode = REQUIRED, description = "List of offender's current alert codes.")
    private List<String> alertsDetails = new ArrayList<>();

    @Schema(description = "Legal Status", name = "legalStatus", example = "REMAND")
    private LegalStatus legalStatus;

    public void deriveLegalDetails() {
        legalStatus = uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus.calcLegalStatus(bandCode, imprisonmentStatus);
        convictedStatus = uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus.calcConvictedStatus(bandCode);
    }
}
