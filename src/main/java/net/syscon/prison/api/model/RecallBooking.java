package net.syscon.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Recall Offender Booking
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Recall Offender Booking")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class RecallBooking {
    @ApiModelProperty(required = true, value = "A unique offender number.")
    @Size(max = 10)
    @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(required = true, value = "The offender's last name.")
    @Size(max = 35)
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "The offender's first name.")
    @Size(max = 35)
    @NotBlank
    private String firstName;

    @ApiModelProperty(required = true, value = "The offender's date of birth. Must be specified in YYYY-MM-DD format.")
    @NotBlank
    private LocalDate dateOfBirth;

    @ApiModelProperty(required = true, value = "A code representing the offender's gender (from the SEX reference domain).")
    @Size(max = 12)
    @NotNull
    private String gender;

    @ApiModelProperty(required = true, value = "A code representing the reason for the offender's recall. 'B' = Recall from HDC. 'Y' = Recall from DTO.", example = "B", allowableValues = "B,Y")
    @Pattern(regexp = "^[BY]$")
    @NotBlank
    private String reason;

    @ApiModelProperty(value = "A flag to indicate that the offender is a youth/young offender (or not). Defaults to false if not specified.")
    private boolean youthOffender;

    @ApiModelProperty(value = "Prison ID (Agency ID) of where to place offender", example = "MDI")
    @Size(max = 3)
    private String prisonId;

}
