package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@ApiModel(description = "Prisoner Booking Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrisonerBookingSummary implements Serializable {

    @NotNull
    @ApiModelProperty(required = true, value = "Unique, numeric booking id.", position = 1, example = "1234134")
    private Long bookingId;

    @ApiModelProperty(value = "Book number.", position = 2, example = "A12121")
    private String bookingNo;

    @NotBlank
    @ApiModelProperty(required = true, value = "Prisoner number (e.g. NOMS Number).", position = 3, example = "A1234AA")
    private String offenderNo;

    @NotBlank
    @ApiModelProperty(required = true, value = "Prisoner first name.", position = 4, example = "JOHN")
    private String firstName;

    @ApiModelProperty(value = "Prisoner's middle name.", position = 5, example = "ASHLEY")
    private String middleName;

    @NotBlank
    @ApiModelProperty(required = true, value = "Prisoner's last name.", position = 6, example = "SMITH")
    private String lastName;

    @NotNull
    @ApiModelProperty(required = true, value = "Prisoner's date of birth.", position = 7, example = "1980-05-02")
    private LocalDate dateOfBirth;

    @NotNull
    @ApiModelProperty(required = true, value = "Prisoner's current age.", position = 8, example = "32")
    private Integer age;

    @NotBlank
    @ApiModelProperty(required = true, value = "Identifier of agency that prisoner is associated with.", position = 9, example = "MDI")
    private String agencyId;

    @ApiModelProperty(value = "Identifier of living unit (e.g. cell) that prisoner is assigned to.", position = 10, example = "123123")
    private Long assignedLivingUnitId;

    @ApiModelProperty(value = "Identifier of facial image of prisoner.", position = 11, example = "1241241")
    private Long facialImageId;

    @ApiModelProperty(value = "The imprisonment status of the prisoner", position = 12, example = "SENT")
    private String imprisonmentStatus;

    @ApiModelProperty(value = "Legal Status", name = "legalStatus", position = 13, example = "REMAND")
    private LegalStatus legalStatus;

    @ApiModelProperty(value = "Convicted Status", name = "convictedStatus", position = 14, example = "Convicted", allowableValues = "Convicted,Remand")
    private String convictedStatus;

    @ApiModelProperty(value = "IEP level of the prisoner", position = 15, example = "Basic")
    private String iepLevel;

    @ApiModelProperty(value = "Description of living unit (e.g. cell) that prisoner is assigned to.", position = 16, example = "MDI-1-1-3")
    private String assignedLivingUnitDesc;
}
