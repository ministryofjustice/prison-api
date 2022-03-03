package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction;

@ApiModel(description = "Detail of result of transfer transaction")
@Data
@EqualsAndHashCode
@ToString
@Builder
public class TransferTransactionDetail {
    @ApiModelProperty(value = "Debit Transaction")
    private Transaction debitTransaction;
    @ApiModelProperty(value = "Credit Transaction")
    private Transaction creditTransaction;
    @ApiModelProperty(value = "Transaction Id")
    private Long transactionId;
}
