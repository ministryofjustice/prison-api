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

import java.math.BigDecimal;
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
    private Integer eventId;

    @ApiModelProperty(value = "Payment amount", example = "1.0", position = 7)
    @JsonDeserialize(using = MoneyDeserializer.class)
    private BigDecimal payAmount;

    @ApiModelProperty(value = "Piece work amount", example = "2.50", position = 8)
    @JsonDeserialize(using = MoneyDeserializer.class)
    private BigDecimal pieceWork;

    @ApiModelProperty(value = "Bonus payment", example = "0.55", position = 9)
    @JsonDeserialize(using = MoneyDeserializer.class)
    private BigDecimal bonusPay;
}
