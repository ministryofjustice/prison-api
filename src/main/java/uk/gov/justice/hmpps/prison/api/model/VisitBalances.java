package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Balances of visit orders and privilege visit orders")
@Builder
@Data
public class VisitBalances {

    @Schema(requiredMode = REQUIRED, description = "Balance of visit orders remaining")
    private Integer remainingVo;

    @Schema(requiredMode = REQUIRED, description = "Balance of privilege visit orders remaining")
    public Integer remainingPvo;

    @Schema(description = "Date of last IEP adjustment for Visit orders")
    private LocalDate latestIepAdjustDate;

    @Schema(description = "Date of last IEP adjustment for Privilege Visit orders")
    private LocalDate latestPrivIepAdjustDate;

    public VisitBalances(Integer remainingVo, Integer remainingPvo, LocalDate latestIepAdjustDate, LocalDate latestPrivIepAdjustDate) {
        this.remainingVo = remainingVo;
        this.remainingPvo = remainingPvo;
        this.latestIepAdjustDate = latestIepAdjustDate;
        this.latestPrivIepAdjustDate = latestPrivIepAdjustDate;
    }

    public VisitBalances() {
    }
}
