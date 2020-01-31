package net.syscon.elite.service;

import net.syscon.elite.api.model.Agency;

import java.util.List;

public interface CaseloadToAgencyMappingService {
    /**
     * Find the Agencies in the user's working caseload.
     *
     * @param username
     * @return The set of Agency associated with the working caseload for user.
     */
    List<Agency> agenciesForUsersWorkingCaseload(String username);
}
