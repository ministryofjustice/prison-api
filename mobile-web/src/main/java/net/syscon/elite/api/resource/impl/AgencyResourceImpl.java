package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.resource.AgencyResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AgencyService;
import net.syscon.util.MetaDataFactory;

import javax.ws.rs.Path;
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
        long offset = nvl(pageOffset, 0L);
        long limit = nvl(pageLimit, 10L);

        List<Agency> agencies = agencyService.getAgencies(offset, limit);

        return GetAgenciesResponse.respond200WithApplicationJson(agencies,
                MetaDataFactory.getTotalRecords(agencies), offset, limit);
    }

    @Override
    public GetAgencyResponse getAgency(String agencyId) {
        return GetAgencyResponse.respond200WithApplicationJson(agencyService.getAgency(agencyId));
    }
}
