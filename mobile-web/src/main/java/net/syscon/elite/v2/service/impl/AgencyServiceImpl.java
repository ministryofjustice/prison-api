package net.syscon.elite.v2.service.impl;

import net.syscon.elite.v2.api.model.Agency;
import net.syscon.elite.v2.service.AgencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Agency API (v2) service implementation.
 */
@Service
@Transactional(readOnly = true)
public class AgencyServiceImpl implements AgencyService {
    @Override
    public Agency getAgency(String agencyId) {
        return null;
    }

    @Override
    public List<Agency> findAgenciesByCaseLoad(String caseLoadId, Long offset, Long limit) {
        return new ArrayList<>();
    }
}
