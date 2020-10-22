package uk.gov.justice.hmpps.prison.api.model;

import lombok.Builder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransactionHistory;

@Builder
public class TransactionHistoryDto {

    public static TransactionHistoryDto toDto(TransactionHistory transactionHistory) {
        return TransactionHistoryDto.builder().build();
    }
}