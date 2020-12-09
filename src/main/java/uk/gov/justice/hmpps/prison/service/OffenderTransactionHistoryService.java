package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static uk.gov.justice.hmpps.prison.util.MoneySupport.poundsToPence;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class OffenderTransactionHistoryService {

    public static final Comparator<OffenderTransactionHistory> SORT_BY_MOST_RECENT_DATE_THEN_BY_LATEST_SEQ = Comparator
        .comparing(OffenderTransactionHistory::getEntryDate).reversed()
        .thenComparing(Comparator.comparing(OffenderTransactionHistory::getTransactionEntrySequence).reversed());

    private final String apiCurrency;
    private final OffenderTransactionHistoryRepository historyRepository;


    public OffenderTransactionHistoryService(@Value("${api.currency:GBP}") final String currency,
                                             final OffenderTransactionHistoryRepository historyRepository) {
        this.apiCurrency = currency;
        this.historyRepository = historyRepository;
    }

    @VerifyOffenderAccess
    public List<OffenderTransactionHistoryDto> getTransactionHistory(final String offenderNo,
                                                                     final Optional<String> accountCodeOpl,
                                                                     final Optional<LocalDate> fromDate,
                                                                     final Optional<LocalDate> toDate,
                                                                     final String transactionType) {
        validate(offenderNo, accountCodeOpl, fromDate, toDate);

        final var accountCode = accountCodeOpl
            .map(AccountCode::byCodeName)
            .map(optionalCode -> optionalCode.get().code)
            .orElse(null);

        return historyRepository.getTransactionHistory(offenderNo, accountCode, fromDate.orElse(null), toDate.orElse(null), transactionType)
            .stream()
            .sorted(SORT_BY_MOST_RECENT_DATE_THEN_BY_LATEST_SEQ)
            .map(this::transform)
            .collect(Collectors.toList());
    }

    private void validate(final String offenderNo,
                          final Optional<String> accountCodeOpl,
                          final Optional<LocalDate> fromDate,
                          final Optional<LocalDate> toDate) {

        checkNotNull(offenderNo, "offenderNo can't be null");
        checkNotNull(accountCodeOpl, "accountCode optional can't be null");
        checkNotNull(fromDate, "fromDate optional can't be null");
        checkNotNull(toDate, "toDate optional can't be null");

        if (fromDate.isPresent() && toDate.isPresent()) checkDateRange(fromDate.get(), toDate.get());

        if (accountCodeOpl.isPresent()) {
            boolean isAccountCodeExists = AccountCode.exists(accountCodeOpl.get());
            checkState(isAccountCodeExists, "Unknown account-code " + accountCodeOpl.get());
        }
    }

    private void checkDateRange(final LocalDate fromDate, final LocalDate toDate) {
        var now = LocalDate.now();
        checkState(fromDate.isBefore(toDate) || fromDate.isEqual(toDate), "toDate can't be before fromDate");
        checkState(fromDate.isBefore(now) || fromDate.isEqual(now), "fromDate can't be in the future");
        checkState(toDate.isBefore(now) || toDate.isEqual(now), "toDate can't be in the future");
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
