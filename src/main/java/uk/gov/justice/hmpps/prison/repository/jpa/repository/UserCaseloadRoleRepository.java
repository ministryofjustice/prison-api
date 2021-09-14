package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRole;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRoleIdentity;

@Repository
public interface UserCaseloadRoleRepository extends CrudRepository<UserCaseloadRole, UserCaseloadRoleIdentity>, JpaSpecificationExecutor<UserCaseloadRole> {
    @Modifying
    @Query("delete from UserCaseloadRole ucr where ucr.id.username = :username and ucr.id.caseload = :caseload and ucr.id.roleId = :roleId")
    void deleteRole(String username, String caseload, Long roleId);
}
