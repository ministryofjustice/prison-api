package net.syscon.elite.web.api.resource.impl;


import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.model.Movement;
import net.syscon.elite.web.api.resource.LocationsResource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Component
public class LocationsResourceImpl implements LocationsResource {

	private AgencyLocationService agencyLocationService;

	@Inject
	public void setAgencyLocationService(final AgencyLocationService agencyLocationService) { this.agencyLocationService = agencyLocationService; }


	@Override
	public GetLocationsResponse getLocations(int offset, int limit) throws Exception {
		List<Location> locations = agencyLocationService.getLocations(offset, limit);
		return GetLocationsResponse.withJsonOK(locations);
	}

	@Override
	public GetLocationsByLocationIdResponse getLocationsByLocationId(String locationId) throws Exception {
		return GetLocationsByLocationIdResponse.withJsonOK(agencyLocationService.getLocation(Long.valueOf(locationId)));
	}

	@Override
	public GetLocationsByLocationIdMovementsResponse getLocationsByLocationIdMovements(String locationId, String orderBy, Order order, int offset, int limit) throws Exception {
		List<Movement> movements = new ArrayList<>();
		return GetLocationsByLocationIdMovementsResponse.withJsonOK(movements);
	}

	@Override
	public GetLocationsByLocationIdInmatesResponse getLocationsByLocationIdInmates(String locationId, String orderBy, Order order,int offset, int limit) throws Exception {
		List<AssignedInmate> inmates = agencyLocationService.getInmatesFromLocation(Long.valueOf(locationId), offset, limit);
		return GetLocationsByLocationIdInmatesResponse.withJsonOK(inmates);
	}

}
