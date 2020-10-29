package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.Pk;

public interface OffenderTransactionHistoryRepository extends CrudRepository<OffenderTransactionHistory, Pk> {

    @Query("select h from OffenderTransactionHistory h where h.offender.nomsId = ?1 and h.accountType = ?2 and (h.entryDate between ?3 and ?4)")
    List<OffenderTransactionHistory> findForGivenAccountType(String offenderNo, String accountType, LocalDate fromDate, LocalDate toDate);

    @Query("select h from OffenderTransactionHistory h where h.offender.nomsId = ?1 and (h.entryDate between ?2 and ?3)")
    List<OffenderTransactionHistory> findForAllAccountTypes(String offenderNo, LocalDate fromDat, LocalDate toDate);
}