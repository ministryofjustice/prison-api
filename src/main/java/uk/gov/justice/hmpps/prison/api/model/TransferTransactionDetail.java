package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction;

@Schema(description = "Detail of result of transfer transaction")
@Data
@EqualsAndHashCode
@ToString
@Builder
public class TransferTransactionDetail {
    @Schema(description = "Debit Transaction")
    private Transaction debitTransaction;
    @Schema(description = "Credit Transaction")
    private Transaction creditTransaction;
    @Schema(description = "Transaction Id")
    private Long transactionId;
}
