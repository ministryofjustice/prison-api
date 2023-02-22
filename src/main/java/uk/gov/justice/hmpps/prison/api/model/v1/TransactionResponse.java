package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Schema(description = "Transaction Response")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class TransactionResponse {
    @Schema(description = "ID of created transaction", example = "6179604-1")
    @NotNull
    private String id;
}
