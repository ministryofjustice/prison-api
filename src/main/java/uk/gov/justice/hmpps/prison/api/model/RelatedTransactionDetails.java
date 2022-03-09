package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.util.MoneySupport.MoneyDeserializer;

import java.time.LocalDate;


@Schema(description = "Offender transaction drill down details")
@Data
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatedTransactionDetails {

    @Schema(description = "Transaction details id", example = "1")
    private Long id;

    @Schema(description = "Transaction Id", example = "1")
    private Long transactionId;

    @Schema(description = "Transaction Sequence", example = "1")
    private Long transactionEntrySequence;

    @Schema(description = "Calendar date the payment was processed", example = "2020-10-12")
    private LocalDate calendarDate;

    @Schema(description = "Pay type code", example = "UNEMPLOYED,SESSION,LTSICK,STSICK,MATERNAL,RETIRED,HOSPITAL")
    private String payTypeCode;

    @Schema(description = "Event id the payment is associated with", example = "1")
    private Long eventId;

    @Schema(description = "Payment amount in pence", example = "100")
    @JsonDeserialize(using = MoneyDeserializer.class)
    private Long payAmount;

    @Schema(description = "Piece work amount in pence", example = "250")
    @JsonDeserialize(using = MoneyDeserializer.class)
    private Long pieceWork;

    @Schema(description = "Bonus payment in pence", example = "55")
    @JsonDeserialize(using = MoneyDeserializer.class)
    private Long bonusPay;

    @Schema(description = "Balance at a point in time")
    private Long currentBalance;

    @Schema(description = "Reason for payment",example = "Cleaner HB1 PM")
    private String paymentDescription;

}
