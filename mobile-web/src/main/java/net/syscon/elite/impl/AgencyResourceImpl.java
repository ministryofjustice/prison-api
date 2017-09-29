package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.resource.AgencyResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.AgencyService;
import net.syscon.util.MetaDataFactory;

import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/agencies")
public class AgencyResourceImpl implements AgencyResource {
    private final AgencyService agencyService;

    public AgencyResourceImpl(AgencyService agencyService) {
        this.agencyService = agencyService;
    }
    @Override
    public GetAgenciesResponse getAgencies(Long pageOffset, Long pageLimit) {

        final List<Agency> agencies = agencyService.findAgenciesByUsername(
                UserSecurityUtils.getCurrentUsername(),
                nvl(pageOffset, 0L), nvl(pageLimit, 10L));

        return GetAgenciesResponse.respond200WithApplicationJson(agencies, MetaDataFactory.getTotalRecords(agencies), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
    }

    @Override
    public GetAgencyResponse getAgency(String agencyId) {
        return GetAgencyResponse.respond200WithApplicationJson(agencyService.getAgency(agencyId));
    }

    @Override
    public GetAgencyLocationsResponse getAgencyLocations(String agencyId, Long pageOffset, Long pageLimit) {
        return GetAgencyLocationsResponse.respond200WithApplicationJson(new ArrayList<>(), 0L, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
    }
}
