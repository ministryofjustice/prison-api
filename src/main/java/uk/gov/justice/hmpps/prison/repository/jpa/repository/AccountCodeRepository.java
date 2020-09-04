package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AccountCode;

import java.util.Optional;

public interface AccountCodeRepository extends CrudRepository<AccountCode, Long> {
    Optional<AccountCode> findByCaseLoadTypeAndSubAccountType(String caseloadType, String subAccountType);
}
