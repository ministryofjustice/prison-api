package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.util.MoneySupport.MoneyDeserializer;

import java.time.LocalDate;


@ApiModel(description = "Offender transaction drill down details")
@Data
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatedTransactionDetails {

    @ApiModelProperty(value = "Transaction details id", example = "1", position = 1)
    private Long id;

    @ApiModelProperty(value = "Transaction Id", example = "1", position = 2)
    private Long transactionId;

    @ApiModelProperty(value = "Transaction Sequence", example = "1", position = 3)
    private Long transactionEntrySequence;

    @ApiModelProperty(value = "Calendar date the payment was processed", example = "2020-10-12", position = 4)
    private LocalDate calendarDate;

    @ApiModelProperty(value = "Pay type code", example = "UNEMPLOYED,SESSION,LTSICK,STSICK,MATERNAL,RETIRED,HOSPITAL", position = 5)
    private String payTypeCode;

    @ApiModelProperty(value = "Event id the payment is associated with", example = "1", position = 6)
    private Long eventId;

    @ApiModelProperty(value = "Payment amount in pence", example = "100", position = 7)
    @JsonDeserialize(using = MoneyDeserializer.class)
    private Long payAmount;

    @ApiModelProperty(value = "Piece work amount in pence", example = "250", position = 8)
    @JsonDeserialize(using = MoneyDeserializer.class)
    private Long pieceWork;

    @ApiModelProperty(value = "Bonus payment in pence", example = "55", position = 9)
    @JsonDeserialize(using = MoneyDeserializer.class)
    private Long bonusPay;

    @ApiModelProperty(value = "Balance at a point in time", position = 10)
    private Long currentBalance;

    @ApiModelProperty(value = "Reason for payment",example = "Cleaner HB1 PM", position = 11)
    private String paymentDescription;

}
