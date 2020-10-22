package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.OffenderTransactionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransactionHistoryTransformer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Validated
@Slf4j
public class OffenderTransactionHistoryService {

    private OffenderTransactionRepository repository;

    public List<OffenderTransactionHistoryDto> getTransactionHistory(final Long offenderId,
                                                                     final Optional<String> accountCode,
                                                                     final LocalDate fromDate,
                                                                     final LocalDate toDate) {

        var histories = (List<OffenderTransactionHistory>) accountCode
                .map(code -> repository.findForGivenAccountType(offenderId, code, fromDate, toDate))
                .orElse(repository.findForAllAccountTypes(offenderId, fromDate, toDate));

        return histories.stream().map(OffenderTransactionHistoryTransformer::transform).collect(Collectors.toList());
    }
}
