package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.AgencyResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.LocationGroupService;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/agencies")
public class AgencyResourceImpl implements AgencyResource {
    private final AgencyService agencyService;
    private final LocationGroupService locationGroupService;

    public AgencyResourceImpl(AgencyService agencyService, LocationGroupService locationGroupService) {
        this.agencyService = agencyService;
        this.locationGroupService = locationGroupService;
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
    public GetAgencyLocationsResponse getAgencyLocations(String agencyId, String eventType, String sortFields, Order sortOrder) {
        List<Location> locations = agencyService.getAgencyLocations(agencyId, eventType, sortFields, sortOrder);

        return GetAgencyLocationsResponse.respond200WithApplicationJson(locations);
    }

    @Override
    public GetAvailableLocationGroupsResponse getAvailableLocationGroups(String agencyId) {
        List<LocationGroup> locationGroups = locationGroupService.getLocationGroupsForAgency(agencyId);
        return GetAvailableLocationGroupsResponse.respond200WithApplicationJson(locationGroups);
    }

    @Override
    public GetWhereaboutsResponse getWhereabouts(String agencyId) {
        List<LocationGroup> locationGroups = locationGroupService.getLocationGroupsForAgency(agencyId);
        WhereaboutsConfig whereaboutsConfig = WhereaboutsConfig.builder().enabled(!locationGroups.isEmpty()).build();
        return GetWhereaboutsResponse.respond200WithApplicationJson(whereaboutsConfig);
    }

    @Override
    public GetAgencyEventLocationsResponse getAgencyEventLocations(String agencyId, String sortFields, Order sortOrder) {
        List<Location> locations = agencyService.getAgencyEventLocations(agencyId, sortFields, sortOrder);

        return GetAgencyEventLocationsResponse.respond200WithApplicationJson(locations);
    }

    @Override
    public GetAgencyEventLocationsBookedResponse getAgencyEventLocationsBooked(String agencyId, LocalDate date, TimeSlot timeSlot) {
        List<Location> locations = agencyService.getAgencyEventLocationsBooked(agencyId, date, timeSlot);

        return GetAgencyEventLocationsBookedResponse.respond200WithApplicationJson(locations);
    }

    @Override
    public GetAgenciesByCaseloadResponse getAgenciesByCaseload(String caseload) {
        List<Agency> agenciesByCaseload = agencyService.getAgenciesByCaseload(caseload);
        return GetAgenciesByCaseloadResponse.respond200WithApplicationJson(agenciesByCaseload);
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
