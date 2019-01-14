package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
@ApiModel(description = "Summary of an offender counted as Establishment Roll - In")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

public class OffenderIn {
    @NotBlank
    @ApiModelProperty(required = true, value = "Display Prisoner Number")
    private String offenderNo;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description for Agency travelling from")
    private String fromAgencyDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description for Agency travelling to")
    private String toAgencyDescription;

    @NotNull
    @ApiModelProperty(required = true, value = "Movement time")
    private LocalTime movementTime;

    @NotNull
    @ApiModelProperty(required = true, value = "Description of the offender's (internal) location")
    private String location;
}
