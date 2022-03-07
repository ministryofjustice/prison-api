package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "Offender basic detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InmateBasicDetails {

    @ApiModelProperty(value = "Offender Booking Id", example = "432132")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(value = "Booking Number")
    @NotBlank
    private String bookingNo;

    @ApiModelProperty(required = true, value = "Offender Unique Reference", position = 1, example = "A1234AA")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(required = true, value = "First Name")
    @NotBlank
    private String firstName;

    @ApiModelProperty(value = "Middle Name(s)")
    private String middleName;

    @ApiModelProperty(required = true, value = "Last Name")
    @NotBlank
    private String lastName;

    @ApiModelProperty(value = "Identifier of agency to which the prisoner is associated.")
    @NotBlank
    private String agencyId;

    @ApiModelProperty(value = "Identifier of living unit (e.g. cell) that prisoner is assigned to.")
    private Long assignedLivingUnitId;

    @ApiModelProperty(value = "Description of living unit (e.g. cell) that offender is assigned to.", example = "MDI-1-1-3")
    private String assignedLivingUnitDesc;

    @ApiModelProperty(required = true, value = "Date of Birth of prisoner", example = "1970-03-15")
    @NotNull
    private LocalDate dateOfBirth;
}
