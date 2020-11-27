package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
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

    private final String apiCurrency;
    private final OffenderTransactionHistoryRepository historyRepository;

    public OffenderTransactionHistoryService(@Value("${api.currency:GBP}") final String currency,
                                             final OffenderTransactionHistoryRepository historyRepository) {
        this.apiCurrency = currency;
        this.historyRepository = historyRepository;
    }

    public static final Comparator<OffenderTransactionHistory> SORT_BY_MOST_RECENT_DATE_THEN_BY_LATEST_SEQ = Comparator
            .comparing(OffenderTransactionHistory::getEntryDate).reversed()
            .thenComparing(Comparator.comparing(OffenderTransactionHistory::getTransactionEntrySequence).reversed());

    @VerifyOffenderAccess
    public List<OffenderTransactionHistoryDto> getTransactionHistory(final String offenderNo,
                                                                     final Optional<String> accountCodeOpl,
                                                                     final Optional<LocalDate> fromDateOpl,
                                                                     final Optional<LocalDate> toDateOpl) {
        validate(offenderNo, accountCodeOpl, fromDateOpl, toDateOpl);

        var fromDate = fromDateOpl.orElse(LocalDate.now());
        var toDate = toDateOpl.orElse(LocalDate.now());
        checkDateRange(fromDate, toDate);

        if (accountCodeOpl.isPresent()) {
            boolean isAccountCodeExists = AccountCode.exists(accountCodeOpl.get());
            checkState(isAccountCodeExists, "Unknown account-code " + accountCodeOpl.get());
        }

        return accountCodeOpl.map(AccountCode::byCodeName)
            .filter(Optional::isPresent)
            .map(optionalCode -> optionalCode.get().code)
            .map(code -> historyRepository.findForGivenAccountType(offenderNo, code, fromDate, toDate))
            .orElse(historyRepository.findForAllAccountTypes(offenderNo, fromDate, toDate))
            .stream()
            .sorted(SORT_BY_MOST_RECENT_DATE_THEN_BY_LATEST_SEQ)
            .map(this::transform)
            .collect(Collectors.toList());
    }

    private void checkDateRange(LocalDate fromDate, LocalDate toDate) {
        var now = LocalDate.now();
        checkState(fromDate.isBefore(toDate) || fromDate.isEqual(toDate), "toDate can't be before fromDate");
        checkState(fromDate.isBefore(now) || fromDate.isEqual(now), "fromDate can't be in the future");
        checkState(toDate.isBefore(now) || toDate.isEqual(now), "toDate can't be in the future");
    }

    private void validate(final String offenderNo,
                          final Optional<String> accountCodeOpl,
                          final Optional<LocalDate> fromDateOpl,
                          final Optional<LocalDate> toDateOpl) {

        checkNotNull(offenderNo, "offenderNo can't be null");
        checkNotNull(accountCodeOpl, "accountCode optional can't be null");
        checkNotNull(fromDateOpl, "fromDate optional can't be null");
        checkNotNull(toDateOpl, "toDate optional can't be null");
    }

    public OffenderTransactionHistoryDto transform(final OffenderTransactionHistory offenderTransactionHistory) {
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
            .transactionType(offenderTransactionHistory.getTransactionType()).build();
    }
}
