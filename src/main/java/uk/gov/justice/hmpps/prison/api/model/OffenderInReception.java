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

@ApiModel(description = "Summary of an offender counted as Establishment Roll - Reception")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderInReception {

    @NotBlank
    @ApiModelProperty(required = true, value = "Display Prisoner Number")
    private String offenderNo;

    @NotBlank
    @ApiModelProperty(required = true, value = "Booking Id")
    private Long bookingId;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
