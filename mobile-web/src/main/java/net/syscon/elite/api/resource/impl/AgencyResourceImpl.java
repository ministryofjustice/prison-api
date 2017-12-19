package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.resource.AgencyResource;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AgencyService;

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
        Page<Agency> agencies = agencyService.getAgencies(nvl(pageOffset, 0L), nvl(pageLimit, 10L));

        return GetAgenciesResponse.respond200WithApplicationJson(agencies);
    }

    @Override
    public GetAgencyResponse getAgency(String agencyId) {
        Agency agency = agencyService.getAgency(agencyId);

        return GetAgencyResponse.respond200WithApplicationJson(agency);
    }

    @Override
    public GetAvailableLocationsResponse getAvailableLocations(String agencyId, String eventType) {
        List<Location> locations = agencyService.getAvailableLocations(agencyId, eventType);

        return GetAvailableLocationsResponse.respond200WithApplicationJson(locations);
    }

    @Override
    public GetPrisonContactDetailListResponse getPrisonContactDetailList() {
        final List<PrisonContactDetail> prisonContactDetail = agencyService.getPrisonContactDetail();
        return GetPrisonContactDetailListResponse.respond200WithApplicationJson(prisonContactDetail);
    }

    @Override
    public GetPrisonContactDetailResponse getPrisonContactDetail(String agencyId) {
        final PrisonContactDetail prisonContactDetail = agencyService.getPrisonContactDetail(agencyId);
        return GetPrisonContactDetailResponse.respond200WithApplicationJson(prisonContactDetail);
    }
}
