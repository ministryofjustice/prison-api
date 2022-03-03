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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Offender Booking Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderBooking implements CategoryCodeAware {

    @NotNull
    @ApiModelProperty(required = true, value = "Unique, numeric booking id.", position = 1, example = "1234134")
    private Long bookingId;

    @ApiModelProperty(value = "Booking number.", position = 2, example = "A12121")
    private String bookingNo;

    @NotBlank
    @ApiModelProperty(required = true, value = "Offender number (e.g. NOMS Number).", position = 3, example = "A1234AA")
    private String offenderNo;

    @NotBlank
    @ApiModelProperty(required = true, value = "Offender first name.", position = 4, example = "JOHN")
    private String firstName;

    @ApiModelProperty(value = "Offender middle name.", position = 5, example = "ASHLEY")
    private String middleName;

    @NotBlank
    @ApiModelProperty(required = true, value = "Offender last name.", position = 6, example = "SMITH")
    private String lastName;

    @NotNull
    @ApiModelProperty(required = true, value = "Offender date of birth.", position = 7, example = "1980-05-02")
    private LocalDate dateOfBirth;

    @NotNull
    @ApiModelProperty(required = true, value = "Offender's current age.", position = 8, example = "32")
    private Integer age;

    @NotBlank
    @ApiModelProperty(required = true, value = "Identifier of agency that offender is associated with.", position = 11, example = "MDI")
    private String agencyId;

    @ApiModelProperty(value = "Identifier of living unit (e.g. cell) that offender is assigned to.", position = 12, example = "123123")
    private Long assignedLivingUnitId;

    @ApiModelProperty(value = "Description of living unit (e.g. cell) that offender is assigned to.", position = 13, example = "MDI-1-1-3")
    private String assignedLivingUnitDesc;

    @ApiModelProperty(value = "Identifier of facial image of offender.", position = 14, example = "1241241")
    private Long facialImageId;

    @ApiModelProperty(value = "Identifier of officer (key worker) to which offender is assigned.", position = 15, example = "354543")
    private String assignedOfficerUserId;

    @ApiModelProperty(value = "List of offender's alias names.", position = 16, allowEmptyValue = true)
    private List<String> aliases;

    @ApiModelProperty(value = "The IEP Level of the offender (UK Only)", position = 17, example = "Basic")
    private String iepLevel;

    @ApiModelProperty(value = "The Cat A/B/C/D of the offender", position = 18, example = "C", allowableValues = "A,B,C,D,I,J")
    private String categoryCode;

    @ApiModelProperty(value = "Convicted Status", name = "convictedStatus", position = 19, example = "Convicted", allowableValues = "Convicted,Remand")
    private String convictedStatus;

    @JsonIgnore
    private String bandCode;

    @ApiModelProperty(value = "The imprisonment status of the offender", position = 20, example = "SENT")
    private String imprisonmentStatus;

    @NotNull
    @Builder.Default
    @ApiModelProperty(required = true, value = "List of offender's current alert types.", position = 21)
    private List<String> alertsCodes = new ArrayList<>();

    @NotNull
    @Builder.Default
    @ApiModelProperty(required = true, value = "List of offender's current alert codes.", position = 22)
    private List<String> alertsDetails = new ArrayList<>();

    @ApiModelProperty(value = "Legal Status", name = "legalStatus", position = 23, example = "REMAND")
    private LegalStatus legalStatus;

    public void deriveLegalDetails() {
        legalStatus = uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus.calcLegalStatus(bandCode, imprisonmentStatus);
        convictedStatus = uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus.calcConvictedStatus(bandCode);
    }
}
