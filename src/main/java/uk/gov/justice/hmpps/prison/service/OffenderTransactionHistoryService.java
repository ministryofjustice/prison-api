package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.OffenderTransactionHistoryRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransactionHistoryTransformer;
import uk.gov.justice.hmpps.prison.values.AccountCode;
import uk.gov.justice.hmpps.prison.values.Currency;

import java.time.LocalDate;
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
    private OffenderTransactionHistoryRepository repository;

    public OffenderTransactionHistoryService(@Value("${api.currency:GBP}") final String currency,
                                             final OffenderTransactionHistoryRepository repository) {
        this.apiCurrency = currency;
        this.repository = repository;
    }

    @VerifyOffenderAccess
    public List<OffenderTransactionHistoryDto> getTransactionHistory(final Long offenderId,
                                                                     final Optional<String> accountCodeOpl,
                                                                     final Optional<LocalDate> fromDateOpl,
                                                                     final Optional<LocalDate> toDateOpl) {

        checkNotNull(offenderId, "offender-id can't be null");
        checkNotNull(accountCodeOpl, "accountCode optional can't be null");
        checkNotNull(fromDateOpl, "fromDate optional can't be null");
        checkNotNull(toDateOpl, "toDate optional can't be null");

        var fromDate = fromDateOpl.orElse(LocalDate.now());
        var toDate = toDateOpl.orElse(LocalDate.now());

        var now = LocalDate.now();
        checkState(fromDate.isBefore(toDate) || fromDate.isEqual(toDate), "toDate can't be before fromDate");
        checkState(fromDate.isBefore(now) || fromDate.isEqual(now), "fromDate can't be in the future");
        checkState(toDate.isBefore(now) || toDate.isEqual(now), "toDate can't be in the future");

        if(accountCodeOpl.isPresent()) {
            boolean isAccountCodeExists = accountCodeOpl.map(AccountCode::byCodeName).filter(opl -> opl.isPresent()).isPresent();
            checkState(isAccountCodeExists, "Unknown account-code " + accountCodeOpl.get());
        }

        var histories = (List<OffenderTransactionHistory>) accountCodeOpl
                .map(AccountCode::byCodeName)
                .filter(Optional::isPresent)
                .map(optionalCode -> optionalCode.get().code)
                .map(code -> repository.findForGivenAccountType(offenderId, code, fromDate, toDate))
                .orElse(repository.findForAllAccountTypes(offenderId, fromDate, toDate));

        var sortPolicy = Comparator
                .comparing(OffenderTransactionHistory::getEntryDate)
                .thenComparing(Comparator.comparing(OffenderTransactionHistory::getTransactionEntrySequence).reversed())
                .reversed();

        Collections.sort(histories, sortPolicy);

        Currency currency = Currency.byCode(apiCurrency).orElse(Currency.GBP);

        return histories.stream()
                .map(h -> Pair.of(h, currency))
                .map(OffenderTransactionHistoryTransformer::transform)
                .collect(Collectors.toList());
    }
}
