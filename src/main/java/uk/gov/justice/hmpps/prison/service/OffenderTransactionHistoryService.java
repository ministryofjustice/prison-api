package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.OffenderTransactionHistoryRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransactionHistoryTransformer;
import uk.gov.justice.hmpps.prison.values.AccountCode;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class OffenderTransactionHistoryService {

    private String apiCurrency;
    private OffenderTransactionHistoryRepository historyRepository;
    private OffenderRepository offenderRepository;

    public OffenderTransactionHistoryService(@Value("${api.currency:GBP}") final String currency,
                                             final OffenderTransactionHistoryRepository historyRepository,
                                             final OffenderRepository offenderRepository) {
        this.apiCurrency = currency;
        this.historyRepository = historyRepository;
        this.offenderRepository = offenderRepository;
    }

    public static final Comparator<OffenderTransactionHistory> TRANSACTION_HISTORY_SORTING_POLICY = Comparator
            .comparing(OffenderTransactionHistory::getEntryDate)
            .thenComparing(Comparator.comparing(OffenderTransactionHistory::getTransactionEntrySequence).reversed())
            .reversed();

    @VerifyOffenderAccess
    public List<OffenderTransactionHistoryDto> getTransactionHistory(final String offenderNo,
                                                                     final Optional<String> accountCodeOpl,
                                                                     final Optional<LocalDate> fromDateOpl,
                                                                     final Optional<LocalDate> toDateOpl) {
       validate(offenderNo, accountCodeOpl, fromDateOpl,toDateOpl);

        Offender offender = Optional.of(offenderRepository.findByNomsId(offenderNo))
               .stream()
               .filter(list -> list.size() > 0)
               .flatMap(Collection::stream)
               .findFirst()
               .orElseThrow(EntityNotFoundException.withMessage("OffenderNo %s not found", offenderNo));

        var fromDate = fromDateOpl.orElse(LocalDate.now());
        var toDate = toDateOpl.orElse(LocalDate.now());
        checkDateRange(fromDate, toDate);

        if(accountCodeOpl.isPresent()) {
            boolean isAccountCodeExists = AccountCode.exists(accountCodeOpl.get());
            checkState(isAccountCodeExists, "Unknown account-code " + accountCodeOpl.get());
        }

        return getSortedHistories(offender.getId(), accountCodeOpl, fromDate, toDate).stream()
                .map(h -> Pair.of(h, apiCurrency))
                .map(OffenderTransactionHistoryTransformer::transform)
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

    private List<OffenderTransactionHistory> getSortedHistories(final Long offenderNo,
                                                                final Optional<String> accountCodeOpl,
                                                                final LocalDate fromDate,
                                                                final LocalDate toDate) {
        var histories = accountCodeOpl
                .map(AccountCode::byCodeName)
                .filter(Optional::isPresent)
                .map(optionalCode -> optionalCode.get().code)
                .map(code -> historyRepository.findForGivenAccountType(offenderNo, code, fromDate, toDate))
                .orElse(historyRepository.findForAllAccountTypes(offenderNo, fromDate, toDate));

        Collections.sort(histories, TRANSACTION_HISTORY_SORTING_POLICY);

        return histories;
    }
}
