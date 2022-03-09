package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "Payment Response")
@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class PaymentResponse {
    @Schema(description = "Message returned from a payment", example = "Payment accepted")
    @NotNull
    private String message;
}
