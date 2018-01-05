package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.resource.LocationResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.LocationService;

import javax.ws.rs.Path;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/locations")
public class LocationsResourceImpl implements LocationResource {
	private final AuthenticationFacade authenticationFacade;
	private final LocationService locationService;

	public LocationsResourceImpl(AuthenticationFacade authenticationFacade, LocationService locationService) {
		this.authenticationFacade = authenticationFacade;
		this.locationService = locationService;
	}

	@Override
	public GetLocationsResponse getLocations(String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<Location> locationsResult = locationService.getLocations(
				authenticationFacade.getCurrentUsername(),
				query,
				sortFields,
				sortOrder,
				nvl(pageOffset, 0L),
				nvl(pageLimit, 10L));

		return GetLocationsResponse.respond200WithApplicationJson(locationsResult);
	}

	@Override
	public GetLocationResponse getLocation(Long locationId) {
		Location location = locationService.getLocation(locationId);

		return GetLocationResponse.respond200WithApplicationJson(location);
	}

	@Override
	public GetOffendersAtLocationResponse getOffendersAtLocation(Long locationId, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<OffenderBooking> inmates = locationService.getInmatesFromLocation(
				locationId,
				authenticationFacade.getCurrentUsername(),
				query,
				sortFields,
				sortOrder,
				nvl(pageOffset, 0L),
				nvl(pageLimit, 10L));

		return GetOffendersAtLocationResponse.respond200WithApplicationJson(inmates);
	}

    @Override
    public GetGroupResponse getGroup(String agencyId, String name) {
        return GetGroupResponse.respond200WithApplicationJson(locationService.getGroup(agencyId, name));
    }

    @Override
    public GetAvailableGroupsResponse getAvailableGroups(String agencyId) {
        return GetAvailableGroupsResponse.respond200WithApplicationJson(locationService.getAvailableGroups(agencyId));
    }
}
