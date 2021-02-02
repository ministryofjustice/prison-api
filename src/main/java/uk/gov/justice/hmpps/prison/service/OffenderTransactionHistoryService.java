package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.api.model.RelatedTransactionDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionHistoryRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.values.AccountCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.justice.hmpps.prison.util.MoneySupport.poundsToPence;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class OffenderTransactionHistoryService {
    public static final Comparator<OffenderTransactionHistory> SORT_BY_RECENT_DATE = Comparator
        .comparing(OffenderTransactionHistory::getCreateDatetime).reversed();

    public static final Comparator<OffenderTransactionHistory> SORT_BY_OLDEST_DATE = Comparator
        .comparing(OffenderTransactionHistory::getCreateDatetime);

    private final String apiCurrency;
    private final OffenderTransactionHistoryRepository historyRepository;


    public OffenderTransactionHistoryService(@Value("${api.currency:GBP}") final String currency,
                                             final OffenderTransactionHistoryRepository historyRepository) {
        this.apiCurrency = currency;
        this.historyRepository = historyRepository;
    }

    @VerifyOffenderAccess
    public List<OffenderTransactionHistoryDto> getTransactionHistory(final String offenderNo,
                                                                     final String accountCode,
                                                                     final LocalDate fromDate,
                                                                     final LocalDate toDate,
                                                                     final String transactionType) {
        validate(offenderNo, accountCode, fromDate, toDate);

        return getAllTransactionsWithRunningBalance(offenderNo)
            .stream()
            .filter(byDateRange(fromDate, toDate))
            .filter(byAccountCode(accountCode))
            .filter(transaction -> transactionType == null || transaction.getTransactionType().equals(transactionType))
            .sorted(SORT_BY_RECENT_DATE)
            .map(this::transform)
            .collect(Collectors.toList());
    }

    private void validate(final String offenderNo, final String accountCode, final LocalDate fromDate, final LocalDate toDate) {
        checkNotNull(offenderNo, "offenderNo can't be null");

        if (accountCode != null) checkState(AccountCode.exists(accountCode), "Unknown account-code " + accountCode);

        if (fromDate != null && toDate != null) {
            final var now = LocalDate.now();
            checkState(fromDate.isBefore(toDate) || fromDate.isEqual(toDate), "toDate can't be before fromDate");
            checkState(fromDate.isBefore(now) || fromDate.isEqual(now), "fromDate can't be in the future");
            checkState(toDate.isBefore(now) || toDate.isEqual(now), "toDate can't be in the future");

        }
    }

    private List<OffenderTransactionHistory> getAllTransactionsWithRunningBalance(final String offenderNo) {
        final var transactions = historyRepository
            .findByOffender_NomsId(offenderNo)
            .stream()
            .sorted(SORT_BY_OLDEST_DATE)
            .collect(groupingBy(transaction -> Pair.of(transaction.getAgencyId(), transaction.getAccountType())));

        return transactions.keySet().stream().flatMap(agencyAccountType -> {
            final var runningBalance = new AtomicReference<>(BigDecimal.valueOf(0));

            transactions.get(agencyAccountType).forEach(transaction -> {
                final var postingType = transaction.getPostingType();
                if (postingType.equals("CR")) {
                    runningBalance.set(runningBalance.get().add(transaction.getEntryAmount()));
                } else if (postingType.equals("DR")) {
                    runningBalance.set(runningBalance.get().subtract(transaction.getEntryAmount()));
                }
                transaction.setCurrentBalance(runningBalance.get());
            });

            return transactions.get(agencyAccountType).stream();
        }).collect(Collectors.toList());
    }

    private Predicate<OffenderTransactionHistory> byDateRange(final LocalDate fromDate, final LocalDate toDate) {
        if (fromDate == null && toDate == null) return entry -> true;

        final var today = LocalDate.now();
        final var from = Optional.ofNullable(fromDate).orElse(today);
        final var to = Optional.ofNullable(toDate).orElse(today);

        return entry -> (entry.getEntryDate().isEqual(from) || entry.getEntryDate().isAfter(from)) &&
            (entry.getEntryDate().isEqual(to) || entry.getEntryDate().isBefore(to));
    }

    private Predicate<OffenderTransactionHistory> byAccountCode(final String accountCode) {
        final var accountCodeValue =
            Optional.ofNullable(accountCode)
                .flatMap(AccountCode::byCodeName)
                .map(optionalCode -> optionalCode.code)
                .orElse(null);

        return entry -> (accountCode == null || entry.getAccountType().equals(accountCodeValue));
    }

    public OffenderTransactionHistoryDto transform(final OffenderTransactionHistory offenderTransactionHistory) {

        final var relatedTransactionDetails = offenderTransactionHistory
            .getRelatedTransactionDetails()
            .stream()
            .map(this::transform)
            .collect(Collectors.toList());

        return OffenderTransactionHistoryDto
            .builder()
            .accountType(offenderTransactionHistory.getAccountType())
            .currency(apiCurrency)
            .penceAmount(poundsToPence(offenderTransactionHistory.getEntryAmount()))
            .entryDate(offenderTransactionHistory.getEntryDate())
            .entryDescription(offenderTransactionHistory.getEntryDescription())
            .offenderId(offenderTransactionHistory.getOffender().getId())
            .offenderNo(offenderTransactionHistory.getOffender().getNomsId())
            .referenceNumber(offenderTransactionHistory.getReferenceNumber())
            .transactionEntrySequence(offenderTransactionHistory.getTransactionEntrySequence())
            .transactionId(offenderTransactionHistory.getTransactionId())
            .postingType(offenderTransactionHistory.getPostingType())
            .agencyId(offenderTransactionHistory.getAgencyId())
            .transactionType(offenderTransactionHistory.getTransactionType())
            .currentBalance(poundsToPence(offenderTransactionHistory.getCurrentBalance()))
            .holdingCleared(offenderTransactionHistory.getHoldingCleared())
            .relatedOffenderTransactions(relatedTransactionDetails)
            .build();
    }

    public RelatedTransactionDetails transform(final OffenderTransactionDetails offenderTransactionDetails) {
        return RelatedTransactionDetails
            .builder()
            .id(offenderTransactionDetails.getId())
            .bonusPay(offenderTransactionDetails.getBonusPay())
            .payAmount(offenderTransactionDetails.getPayAmount())
            .pieceWork(offenderTransactionDetails.getPieceWork())
            .calendarDate(offenderTransactionDetails.getCalendarDate())
            .payTypeCode(offenderTransactionDetails.getPayTypeCode())
            .eventId(offenderTransactionDetails.getEventId())
            .transactionEntrySequence(offenderTransactionDetails.getTransactionEntrySequence())
            .transactionId(offenderTransactionDetails.getTransactionId())
            .build();
    }

}
