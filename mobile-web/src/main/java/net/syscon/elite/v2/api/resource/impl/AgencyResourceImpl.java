package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.v2.api.model.Agency;
import net.syscon.elite.v2.api.resource.AgencyResource;
import net.syscon.elite.v2.service.AgencyService;

import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

@RestResource
@Path("/agencies")
public class AgencyResourceImpl implements AgencyResource {
    // TODO: Fully implement /v2/agencies endpoint as exact requirements become clearer.
    private final AgencyService agencyService;

    public AgencyResourceImpl(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    @Override
    public GetAgenciesResponse getAgencies(Long offset, Long limit) {
        final List<Agency> agencies =
                agencyService.findAgenciesByUsername(
                        UserSecurityUtils.getCurrentUsername(),
                        offset != null ? offset : 0,
                        limit != null ? limit : 10);

        return GetAgenciesResponse.respond200WithApplicationJson(agencies);
    }

    @Override
    public GetAgencyResponse getAgency(String agencyId) {
        return GetAgencyResponse.respond200WithApplicationJson(agencyService.getAgency(agencyId));
    }

    @Override
    public GetAgencyLocationsResponse getAgencyLocations(String agencyId, Long offset, Long limit) {
        return GetAgencyLocationsResponse.respond200WithApplicationJson(new ArrayList<>());
    }
}
