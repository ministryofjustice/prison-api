package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@ApiModel(description = "Offender out today details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OffenderOutTodayDto {

    @NotBlank
    @ApiModelProperty(required = true, value = "Offender Unique Reference")
    private String offenderNo;

    @NotBlank
    @ApiModelProperty(required = true)
    private LocalDate dateOfBirth;

    @ApiModelProperty(value = "Reason for out movement")
    private String reasonDescription;

    @NotBlank
    @ApiModelProperty(required = true)
    private LocalTime timeOut;

    @NotBlank
    @ApiModelProperty(required = true)
    private String firstName;

    @NotBlank
    @ApiModelProperty(required = true)
    private String lastName;
}
