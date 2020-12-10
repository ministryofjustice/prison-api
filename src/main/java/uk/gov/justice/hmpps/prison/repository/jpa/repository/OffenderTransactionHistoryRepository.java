package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.Pk;

public interface OffenderTransactionHistoryRepository extends CrudRepository<OffenderTransactionHistory, Pk> {

    @Query("select h from OffenderTransactionHistory h where h.offender.nomsId = :offenderNo and " +
        " (:accountType is null or h.accountType = :accountType) and " +
        " ((:fromDate is null and :toDate is null) or h.entryDate between :fromDate and :toDate) and" +
        " (:transactionType is null or h.transactionType = :transactionType)")
    List<OffenderTransactionHistory> getTransactionHistory(String offenderNo, String accountType, LocalDate fromDate, LocalDate toDate, String transactionType);
}