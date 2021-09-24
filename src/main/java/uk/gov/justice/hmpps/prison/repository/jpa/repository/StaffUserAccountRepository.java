package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Caseload;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;

import java.util.List;
import java.util.Optional;

public interface StaffUserAccountRepository extends CrudRepository<StaffUserAccount, String> {

    @Query("select distinct c from StaffUserAccount sua join sua.caseloads uc join uc.caseload c where c.active = :active and c.type = :type and sua.username = :username")
    List<Caseload> getCaseloadsForUser(final String username, final boolean active, final String type);

    Optional<StaffUserAccount> findByTypeAndStaff_StaffId(String type, Long staffId);
}
