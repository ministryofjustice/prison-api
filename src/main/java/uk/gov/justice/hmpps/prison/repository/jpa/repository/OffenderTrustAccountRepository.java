package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount.Pk;

public interface OffenderTrustAccountRepository extends CrudRepository<OffenderTrustAccount, Pk> {
}
