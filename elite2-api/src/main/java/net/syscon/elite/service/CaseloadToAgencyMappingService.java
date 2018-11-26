package net.syscon.elite.service;

import net.syscon.elite.api.model.Agency;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

public interface CaseloadToAgencyMappingService {
    /**
     * Find the Agencies in the user's working caseload.
     * @param username
     * @return The set of Agency associated with the working caseload for user.
     */
    List<Agency> agenciesForUsersWorkingCaseload(String username);
}
