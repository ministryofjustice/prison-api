package uk.gov.justice.hmpps.prison.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;

import java.time.LocalDate;
import java.util.List;

public interface OffenderTransactionHistoryRepository extends CrudRepository<OffenderTransactionHistory, String> {

    @Query("select h from OffenderTransactionHistory h where h.offenderId = ?1 and h.accountType = ?2 and (h.entryDate between ?3 and ?4)")
    List<OffenderTransactionHistory> findForGivenAccountType(Long offenderId, String accountType, LocalDate fromDat, LocalDate toDate);

    @Query("select h from OffenderTransactionHistory h where h.offenderId = ?1 and (h.entryDate between ?2 and ?3)")
    List<OffenderTransactionHistory> findForAllAccountTypes(Long offenderId, LocalDate fromDat, LocalDate toDate);
}