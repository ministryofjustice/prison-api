package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Offender fine payments")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderFinePaymentDto {

    @Schema(description = "The bookingId this payment relates to")
    private Long bookingId;

    @Schema(description = "Payment sequence - a unique identifier a payment on a booking")
    private Integer sequence;

    @Schema(description = "The date of the payment")
    private LocalDate paymentDate;

    @Schema(description = "The amount of the payment")
    private BigDecimal paymentAmount;

}
