package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;

import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.Pk;

public interface OffenderTransactionHistoryRepository extends CrudRepository<OffenderTransactionHistory, Pk> {
    List<OffenderTransactionHistory> findByOffender_NomsId(String offenderNo);
}