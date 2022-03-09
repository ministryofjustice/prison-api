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
import java.time.LocalTime;

@ApiModel(description = "Summary of an offender counted as Establishment Roll - In")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor

public class OffenderIn {
    @NotBlank
    @ApiModelProperty(required = true, value = "Display Prisoner Number")
    private String offenderNo;

    @NotNull
    private Long bookingId;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotBlank
    @ApiModelProperty(required = true, value = "Id for Agency travelling from")
    private String fromAgencyId;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description for Agency travelling from")
    private String fromAgencyDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Id for Agency travelling to")
    private String toAgencyId;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description for Agency travelling to")
    private String toAgencyDescription;

    @NotBlank
    @ApiModelProperty(value = "City offender was received from")
    private String fromCity;

    @NotBlank
    @ApiModelProperty(value = "City offender was sent to")
    private String toCity;

    @NotNull
    @ApiModelProperty(required = true, value = "Movement time")
    private LocalTime movementTime;

    @NotNull
    @ApiModelProperty(required = true, value = "Movement date time")
    private LocalDateTime movementDateTime;

    @NotNull
    @ApiModelProperty(required = true, value = "Description of the offender's (internal) location")
    private String location;
}
