package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Account Balance")
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"spends", "savings", "cash"})
public class AccountBalance {

    @Schema(description = "Cash balance", example = "13565")
    private Long cash;

    @Schema(description = "Spends balance", example = "5678")
    private Long spends;

    @Schema(description = "Saving balance", example = "12344")
    private Long savings;
}
