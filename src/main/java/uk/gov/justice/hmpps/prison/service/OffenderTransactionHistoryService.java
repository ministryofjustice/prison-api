package uk.gov.justice.hmpps.prison.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.api.model.RelatedTransactionDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourseAttendance;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionHistoryRepository;
import uk.gov.justice.hmpps.prison.values.AccountCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.hmpps.prison.util.MoneySupport.poundsToPence;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class OffenderTransactionHistoryService {
    private static final Comparator<OffenderTransactionHistory> SORT_BY_RECENT_DATE = Comparator
        .comparing(OffenderTransactionHistory::getCreateDatetime).reversed();

    private static final Comparator<OffenderTransactionHistory> SORT_BY_OLDEST_DATE = Comparator
        .comparing(OffenderTransactionHistory::getCreateDatetime);

    private static final Comparator<OffenderTransactionDetails> SORT_RELATED_BY_RECENT_DATE_THEN_TXN_DETAIL_ID = Comparator
        .comparing(OffenderTransactionDetails::getCalendarDate, Comparator.reverseOrder())
        .thenComparing(OffenderTransactionDetails::getId, Comparator.reverseOrder());

    private final String apiCurrency;
    private final OffenderTransactionHistoryRepository historyRepository;

    private final EntityManager entityManager;

    public OffenderTransactionHistoryService(@Value("${api.currency:GBP}") final String currency,
                                             final OffenderTransactionHistoryRepository historyRepository,
                                             final EntityManager entityManager) {
        this.apiCurrency = currency;
        this.historyRepository = historyRepository;
        this.entityManager = entityManager;
    }

    public List<OffenderTransactionHistoryDto> getTransactionHistory(final String offenderNo,
                                                                     final String accountCode,
                                                                     final LocalDate fromDate,
                                                                     final LocalDate toDate,
                                                                     final String transactionType) {
        validate(offenderNo, accountCode, fromDate, toDate);

        // balance is calculated per transaction by reading every transaction regardless of filter
        var allTransactions = getAllTransactionsWithRunningBalance(offenderNo);

        var filteredTransactions = allTransactions
            .stream()
            .filter(byDateRange(fromDate, toDate))
            .filter(byAccountCode(accountCode))
            .filter(transaction -> transactionType == null || transaction.getTransactionType().equals(transactionType))
            .sorted(SORT_BY_RECENT_DATE)
            .toList();

        // Detach all filtered out transactions from JPA session so no further JPA loads happen on these redundant entities
        allTransactions
            .stream()
            .filter(transaction -> !filteredTransactions.contains(transaction))
            .forEach(entityManager::detach);


        return filteredTransactions
            .stream()
            .map(this::enrichRelatedTransactionsWithCurrentBalance)
            .map(this::transform)
            .collect(toList());
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

        return transactions.keySet().stream().flatMap(agencyAccountTypePair -> {
            final var runningBalance = new AtomicReference<>(BigDecimal.valueOf(0));

            return transactions.get(agencyAccountTypePair).stream().flatMap(transaction -> {
                final var postingType = transaction.getPostingType();

                if (postingType.equals("CR")) {
                    runningBalance.updateAndGet((v) -> v.add(transaction.getEntryAmount()));
                } else if (postingType.equals("DR")) {
                    runningBalance.updateAndGet((v) -> v.subtract(transaction.getEntryAmount()));
                }

                transaction.setCurrentBalance(runningBalance.get());

                return Stream.of(transaction);
            });
        }).collect(toList());
    }

    private Predicate<OffenderTransactionHistory> byDateRange(final LocalDate fromDate, final LocalDate toDate) {
        if (fromDate == null && toDate == null) return entry -> true;

        final var today = LocalDate.now();
        final var from = Optional.ofNullable(fromDate).orElse(today);
        final var to = Optional.ofNullable(toDate).orElse(today);

        return entry -> (entry.getEntryDate().atStartOfDay().isEqual(from.atStartOfDay()) || entry.getEntryDate().atStartOfDay().isAfter(from.atStartOfDay())) &&
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

    private OffenderTransactionHistory enrichRelatedTransactionsWithCurrentBalance(final OffenderTransactionHistory offenderTransactionHistory) {
        var balanceAfterTransaction = new AtomicReference<>(offenderTransactionHistory.getCurrentBalance());
        var orderedRelatedTransactions = offenderTransactionHistory.getRelatedTransactionDetails().stream()
            .sorted(SORT_RELATED_BY_RECENT_DATE_THEN_TXN_DETAIL_ID).collect(toList());
        orderedRelatedTransactions.forEach(t ->
            t.setCurrentBalance(balanceAfterTransaction.getAndUpdate(b -> b.subtract(t.getPayAmount())))
        );
        offenderTransactionHistory.setRelatedTransactionDetails(orderedRelatedTransactions);
        return offenderTransactionHistory;
    }

    private OffenderTransactionHistoryDto transform(final OffenderTransactionHistory offenderTransactionHistory) {

        final var relatedTransactionDetails = offenderTransactionHistory
            .getRelatedTransactionDetails()
            .stream()
            .map(this::transform)
            .collect(toList());

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
            .createDateTime(offenderTransactionHistory.getCreateDatetime())
            .relatedOffenderTransactions(relatedTransactionDetails)
            .build();
    }

    private RelatedTransactionDetails transform(final OffenderTransactionDetails offenderTransactionDetails) {
        final var eventId =
            Optional.ofNullable(offenderTransactionDetails.getEvent())
                .map(OffenderCourseAttendance::getEventId)
                .orElse(null);

        final var eventDescription =
            Optional.ofNullable(offenderTransactionDetails.getEvent())
                .map(OffenderCourseAttendance::getCourseActivity)
                .map(CourseActivity::getDescription)
                .orElse(null);

        final var paymentTypeDescription =
            Optional.of(offenderTransactionDetails)
                .filter(od -> !od.isAttachedToPaidActivity())
                .map(OffenderTransactionDetails::getNoneActivityPaymentType)
                .map(ReferenceCode::getDescription)
                .orElse(eventDescription);

        final var paymentTypeCode =
            Optional.of(offenderTransactionDetails)
                .filter(od -> !od.isAttachedToPaidActivity())
                .map(OffenderTransactionDetails::getNoneActivityPaymentType)
                .map(ReferenceCode::getCode)
                .orElse(OffenderTransactionDetails.ACTIVITY_PAY_TYPE_CODE);

        return RelatedTransactionDetails
            .builder()
            .id(offenderTransactionDetails.getId())
            .bonusPay(poundsToPence(offenderTransactionDetails.getBonusPay()))
            .payAmount(poundsToPence(offenderTransactionDetails.getPayAmount()))
            .pieceWork(poundsToPence(offenderTransactionDetails.getPieceWork()))
            .currentBalance(poundsToPence(offenderTransactionDetails.getCurrentBalance()))
            .calendarDate(offenderTransactionDetails.getCalendarDate())
            .payTypeCode(paymentTypeCode)
            .transactionEntrySequence(offenderTransactionDetails.getTransactionEntrySequence())
            .transactionId(offenderTransactionDetails.getTransactionId())
            .paymentDescription(paymentTypeDescription)
            .eventId(eventId)
            .build();
    }
}
