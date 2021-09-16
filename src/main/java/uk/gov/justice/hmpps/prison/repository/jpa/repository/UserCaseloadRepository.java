package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseload;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadId;

@Repository
public interface UserCaseloadRepository extends CrudRepository<UserCaseload, UserCaseloadId>, JpaSpecificationExecutor<UserCaseload> {

}
