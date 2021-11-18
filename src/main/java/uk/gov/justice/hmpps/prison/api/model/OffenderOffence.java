package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel(description = "Offence details related to an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderOffence {
    @ApiModelProperty(value = "Internal ID for charge relating to offender")
    private Long offenderChargeId;
    @ApiModelProperty(value = "Offence Start Date")
    private LocalDate offenceStartDate;
    @ApiModelProperty(value = "Offence End Date")
    private LocalDate offenceEndDate;
    @ApiModelProperty(value = "Offence Code")
    private String offenceCode;
    @ApiModelProperty(value = "Offence Description")
    private String offenceDescription;
}
