package uk.gov.justice.hmpps.prison.service.transformers;

import org.springframework.data.util.Pair;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;
import uk.gov.justice.hmpps.prison.values.Currency;

import static uk.gov.justice.hmpps.prison.util.MoneySupport.poundsToPence;

public class OffenderTransactionHistoryTransformer {

    public static OffenderTransactionHistoryDto transform(final OffenderTransactionHistory history) {
        return transform(Pair.of(history, Currency.GBP));
    }

    public static OffenderTransactionHistoryDto transform(final Pair<OffenderTransactionHistory, Currency> pair) {
        return OffenderTransactionHistoryDto
                .builder()
                .accountType(pair.getFirst().getAccountType())
                .currency(pair.getSecond().code)
                .penceAmount(poundsToPence(pair.getFirst().getEntryAmount()))
                .entryDate(pair.getFirst().getEntryDate())
                .entryDescription(pair.getFirst().getEntryDescription())
                .offenderId(pair.getFirst().getOffenderId())
                .referenceNumber(pair.getFirst().getReferenceNumber())
                .transactionEntrySequence(pair.getFirst().getTransactionEntrySequence())
                .transactionId(pair.getFirst().getTransactionId())
                .transactionType(pair.getFirst().getTransactionType()).build();
    }
}
