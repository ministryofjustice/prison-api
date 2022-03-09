package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "Balances of visit orders and privilege visit orders")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitBalances {

    @Schema(required = true, description = "Balance of visit orders remaining")
    private Integer remainingVo;

    @Schema(required = true, description = "Balance of privilege visit orders remaining")
    public Integer remainingPvo;

    @Schema(description = "Date of last IEP adjustment for Visit orders")
    private LocalDate latestIepAdjustDate;

    @Schema(description = "Date of last IEP adjustment for Privilege Visit orders")
    private LocalDate latestPrivIepAdjustDate;
}
