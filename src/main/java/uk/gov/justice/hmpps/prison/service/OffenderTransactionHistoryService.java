package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.repository.OffenderTransactionHistoryRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransactionHistory;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Validated
@Slf4j
public class OffenderTransactionHistoryService {

    OffenderTransactionHistoryRepository offenderTransactionHistoryRepository;

    public List<TransactionHistory> getTransactionHistory(final String prisonId,
                                                          final String nomisId,
                                                          final String accountCode,
                                                          final LocalDate fromDate,
                                                          final LocalDate toDate) {
        return offenderTransactionHistoryRepository.getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate);
    }
}
