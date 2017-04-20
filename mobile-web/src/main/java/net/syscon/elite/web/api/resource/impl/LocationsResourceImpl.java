package net.syscon.elite.web.api.resource.impl;


import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.resource.LocationsResource;


@Component
public class LocationsResourceImpl implements LocationsResource {

	private AgencyLocationService agencyLocationService;

	@Inject
	public void setAgencyLocationService(final AgencyLocationService agencyLocationService) { this.agencyLocationService = agencyLocationService; }

	@Override
	public GetLocationsResponse getLocations(final String query, final String orderBy, final Order order, final int offset, final int limit) throws Exception {
		final List<Location> locations = agencyLocationService.getLocations(offset, limit);
		return GetLocationsResponse.withJsonOK(locations);
	}

	@Override
	public GetLocationsByLocationIdResponse getLocationsByLocationId(final String locationId) throws Exception {
		return GetLocationsByLocationIdResponse.withJsonOK(agencyLocationService.getLocation(Long.valueOf(locationId)));
	}

	@Override
	public GetLocationsByLocationIdInmatesResponse getLocationsByLocationIdInmates(final String locationId, final String query, final String orderBy, final Order order, final int offset, final int limit) throws Exception {
		final List<AssignedInmate> inmates = agencyLocationService.getInmatesFromLocation(Long.valueOf(locationId), offset, limit);
		return GetLocationsByLocationIdInmatesResponse.withJsonOK(inmates);
	}



}
