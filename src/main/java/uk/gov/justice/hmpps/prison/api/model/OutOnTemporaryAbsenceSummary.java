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
import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@ApiModel(description = "Summary data for a completed movement")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OutOnTemporaryAbsenceSummary {

    @ApiModelProperty(required = true, value = "Offender number (NOMS ID)", example = "G3878UK", position = 0)
    private String offenderNo;

    @NotBlank
    @ApiModelProperty(required = true, value = "Prisoner first name.", example = "JOHN", position = 1)
    private String firstName;

    @NotBlank
    @ApiModelProperty(required = true, value = "Prisoner's last name.", example = "SMITH", position = 2)
    private String lastName;

    @NotNull
    @ApiModelProperty(required = true, value = "Prisoner's date of birth.", example = "1980-05-02", position = 3)
    private LocalDate dateOfBirth;

    @ApiModelProperty(required = true, value = "Movement date and time in Europe/London local time format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00", position = 4)
    private LocalDateTime movementTime;

    @ApiModelProperty(value = "Agency travelling to", example = "MDI", position = 5)
    private String toAgency;

    @ApiModelProperty( value = "Description of the agency travelling to", example = "HMP MOORLANDS", position = 7)
    private String toAgencyDescription;

    @ApiModelProperty(value = "City offender was sent to", example = "DONCASTER", position = 8)
    private String toCity;

    @ApiModelProperty(required = true, value = "The movement reason code", example = "C1", position = 9)
    private String movementReasonCode;

    @ApiModelProperty(required = true, value = "Description of movement reason", example = "Convicted at court", position = 10)
    private String movementReason;

    @ApiModelProperty(value = "Comment", example = "This is a free text comment", position = 11)
    private String commentText;
}
