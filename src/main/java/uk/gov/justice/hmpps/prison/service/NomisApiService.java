package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.TransactionHistory;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;

import java.time.LocalDate;

@Slf4j
@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('SYSTEM_USER','NOMIS_API_V1')")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class NomisApiService {

    private final FinanceRepository financeRepository;

    public TransactionHistory getTransactionsHistory(final String prisonId, final String nomisId, final String accountCode, final LocalDate fromDate, final LocalDate toDate) {
        return TransactionHistory.builder()
                .items(financeRepository.getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate))
                .build();
    }
}