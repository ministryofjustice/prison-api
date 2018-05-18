package net.syscon.elite.service;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;

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

    /**
     * Add an 'access' role - a role assigned to the special 'API Caseload'.
     * @param username The user to whom the role is being assigned
     * @param roleCode The role to assign
     * @return true if the role was added, false if the role assignment already exists (no change).
     */

    boolean addAccessRole(String username, String roleCode);

    /**
     * add all active users with a specified caseload to the default API caseload
     * @param caseloadId
     * @return number of users added to the api caseload
     */
    int addDefaultCaseloadForPrison(String caseloadId);
}
