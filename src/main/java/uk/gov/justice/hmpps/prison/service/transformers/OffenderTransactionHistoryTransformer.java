package uk.gov.justice.hmpps.prison.service.transformers;

import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;

import static uk.gov.justice.hmpps.prison.util.MoneySupport.poundsToPence;

public class OffenderTransactionHistoryTransformer {

    public static OffenderTransactionHistoryDto transform(final OffenderTransactionHistory entity) {
        return OffenderTransactionHistoryDto
                .builder()
                .accountType(entity.getAccountType())
                .penceAmount(poundsToPence(entity.getEntryAmount()))
                .entryDate(entity.getEntryDate())
                .entryDescription(entity.getEntryDescription())
                .offenderId(entity.getOffenderId())
                .referenceNumber(entity.getReferenceNumber())
                .transactionEntrySequence(entity.getTransactionEntrySequence())
                .transactionId(entity.getTransactionId())
                .transactionType(entity.getTransactionType()).build();
    }
}
