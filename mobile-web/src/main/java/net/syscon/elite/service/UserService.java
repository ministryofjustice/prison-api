package net.syscon.elite.service;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;

import java.util.List;
import java.util.Set;

public interface UserService {
    String STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION = "GENERAL";

    UserDetail getUserByUsername(String username);

    List<CaseLoad> getCaseLoads(String username, boolean allCaseloads);

    Set<String> getCaseLoadIds(String username);

    void setActiveCaseLoad(String username, String caseLoadId);

    List<UserRole> getRolesByUsername(String username, boolean allRoles);

    UserDetail getUserByExternalIdentifier(String idType, String id, boolean activeOnly);

    Set<String> getAllUsernamesForCaseloadAndRole(String caseload, String roleCode);

    void removeUsersAccessRoleForCaseload(String username, String caseload, String roleCode);

    boolean isUserAssessibleCaseloadAvailable(String caseload, String username);

    /**
     * Add an 'access' role - using the 'API Caseload'.
     * @param username The user to whom the role is being assigned
     * @param roleCode The role to assign
     * @return true if the role was added, false if the role assignment already exists (no change).
     */

    boolean addAccessRole(String username, String roleCode);

    boolean addAccessRole(String username, String roleCode, String caseloadId);

    /**
     * add all active users with a specified caseload to the default API caseload
     * @param caseloadId the id for the caseload
     * @return number of users added to the api caseload
     */
    int addDefaultCaseloadForPrison(String caseloadId);

    Page<UserDetail> getUsersByCaseload(String caseload, String nameFilter, String accessRole, PageRequest pageRequest);

    List<UserRole> getAccessRolesByUserAndCaseload(String username, String caseload);
}
