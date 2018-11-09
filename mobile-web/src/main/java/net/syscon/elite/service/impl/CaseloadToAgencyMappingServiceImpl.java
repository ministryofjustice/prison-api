package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.CaseloadToAgencyMappingService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CaseloadToAgencyMappingServiceImpl implements CaseloadToAgencyMappingService {

    private final AgencyService agencyService;
    private final CaseLoadService caseLoadService;

    public CaseloadToAgencyMappingServiceImpl(AgencyService agencyService, CaseLoadService caseLoadService) {
        this.agencyService = agencyService;
        this.caseLoadService = caseLoadService;
    }

    @Override
    public List<Agency> agenciesForUsersWorkingCaseload(String username) {
            return caseLoadService
                    .getWorkingCaseLoadForUser(username)
                    .map(CaseLoad::getCaseLoadId)
                    .map(agencyService::getAgenciesByCaseload)
                    .orElse(Collections.emptyList());
    }
}

