package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Caseload;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;

import java.util.List;

public interface StaffUserAccountRepository extends CrudRepository<StaffUserAccount, String> {

    @Query("select distinct c from StaffUserAccount sua join sua.caseloads uc join uc.caseload c where c.activeFlag = :activeFlag and c.type = :type and sua.username = :username")
    List<Caseload> getCaseloadsForUser(final String username, final ActiveFlag activeFlag, final String type);

}
