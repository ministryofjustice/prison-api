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
}
