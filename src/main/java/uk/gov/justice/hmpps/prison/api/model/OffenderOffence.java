package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Offence details related to an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderOffence {
    @Schema(description = "Internal ID for charge relating to offender")
    private Long offenderChargeId;
    @Schema(description = "Offence Start Date")
    private LocalDate offenceStartDate;
    @Schema(description = "Offence End Date")
    private LocalDate offenceEndDate;
    @Schema(description = "Offence Code")
    private String offenceCode;
    @Schema(description = "Offence Description")
    private String offenceDescription;
    @Schema(description = "Offence Indicators")
    private List<String> indicators;
}
