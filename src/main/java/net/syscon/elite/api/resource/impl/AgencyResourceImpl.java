package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.WhereaboutsConfig;
import net.syscon.elite.api.resource.AgencyResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.LocationGroupService;
import net.syscon.elite.service.WhereaboutsEnabledService;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.ws.rs.Path;
import java.time.LocalDate;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/agencies")
public class AgencyResourceImpl implements AgencyResource {
    private final AgencyService agencyService;
    private final LocationGroupService locationGroupService;
    private final WhereaboutsEnabledService whereaboutsEnabledService;

    public AgencyResourceImpl(
            final AgencyService agencyService,
            @Qualifier("locationGroupServiceSelector") final LocationGroupService locationGroupService,
            WhereaboutsEnabledService whereaboutsEnabledService) {
        this.agencyService = agencyService;
        this.locationGroupService = locationGroupService;
        this.whereaboutsEnabledService = whereaboutsEnabledService;
    }

    @Override
    public GetAgenciesResponse getAgencies(final Long pageOffset, final Long pageLimit) {
        final var agencies = agencyService.getAgencies(nvl(pageOffset, 0L), nvl(pageLimit, 10L));

        return GetAgenciesResponse.respond200WithApplicationJson(agencies);
    }

    @Override
    public GetAgencyResponse getAgency(final String agencyId, final boolean activeOnly) {
        final var agency = agencyService.getAgency(agencyId, activeOnly);

        return GetAgencyResponse.respond200WithApplicationJson(agency);
    }

    @Override
    public GetAgencyLocationsResponse getAgencyLocations(final String agencyId, final String eventType, final String sortFields, final Order sortOrder) {
        final var locations = agencyService.getAgencyLocations(agencyId, eventType, sortFields, sortOrder);

        return GetAgencyLocationsResponse.respond200WithApplicationJson(locations);
    }

    @Override
    public GetAvailableLocationGroupsResponse getAvailableLocationGroups(final String agencyId) {
        final var locationGroups = locationGroupService.getLocationGroupsForAgency(agencyId);
        return GetAvailableLocationGroupsResponse.respond200WithApplicationJson(locationGroups);
    }

    @Override
    public WhereaboutsConfig getWhereabouts(final String agencyId) {
        return WhereaboutsConfig.builder().enabled(whereaboutsEnabledService.isEnabled(agencyId)).build();
    }

    @Override
    public GetAgencyEventLocationsResponse getAgencyEventLocations(final String agencyId, final String sortFields, final Order sortOrder) {
        final var locations = agencyService.getAgencyEventLocations(agencyId, sortFields, sortOrder);

        return GetAgencyEventLocationsResponse.respond200WithApplicationJson(locations);
    }

    @Override
    public GetAgencyEventLocationsBookedResponse getAgencyEventLocationsBooked(final String agencyId, final LocalDate date, final TimeSlot timeSlot) {
        final var locations = agencyService.getAgencyEventLocationsBooked(agencyId, date, timeSlot);

        return GetAgencyEventLocationsBookedResponse.respond200WithApplicationJson(locations);
    }

    @Override
    public GetAgenciesByCaseloadResponse getAgenciesByCaseload(final String caseload) {
        final var agenciesByCaseload = agencyService.getAgenciesByCaseload(caseload);
        return GetAgenciesByCaseloadResponse.respond200WithApplicationJson(agenciesByCaseload);
    }

    @Override
    public GetPrisonContactDetailListResponse getPrisonContactDetailList() {
        final var prisonContactDetail = agencyService.getPrisonContactDetail();
        return GetPrisonContactDetailListResponse.respond200WithApplicationJson(prisonContactDetail);
    }

    @Override
    public GetPrisonContactDetailResponse getPrisonContactDetail(final String agencyId) {
        final var prisonContactDetail = agencyService.getPrisonContactDetail(agencyId);
        return GetPrisonContactDetailResponse.respond200WithApplicationJson(prisonContactDetail);
    }
}
