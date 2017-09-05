package net.syscon.elite.web.api.resource.impl;


import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.InmateSummaries;
import net.syscon.elite.web.api.model.InmatesSummary;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.model.Locations;
import net.syscon.elite.web.api.resource.LocationsResource;
import net.syscon.util.MetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Path;
import java.util.List;


@RestResource
@Path("/locations")
public class LocationsResourceImpl implements LocationsResource {
	@Autowired
	private AgencyLocationService agencyLocationService;

	@Override
	public GetLocationsResponse getLocations(final String query, final String orderBy, final Order order, final int offset, final int limit) throws Exception {
		final List<Location> locationsResult = agencyLocationService.getLocations(query, orderBy, order, offset, limit);
		Locations locations = new Locations(locationsResult, MetaDataFactory.createMetaData(limit, offset, locationsResult));
		return GetLocationsResponse.withJsonOK(locations);
	}

	@Override
	public GetLocationsByLocationIdResponse getLocationsByLocationId(final String locationId) throws Exception {
		return GetLocationsByLocationIdResponse.withJsonOK(agencyLocationService.getLocation(Long.valueOf(locationId), false));
	}

	@Override
	public GetLocationsByLocationIdInmatesResponse getLocationsByLocationIdInmates(final String locationId, final String query, final String orderBy, final Order order, final int offset, final int limit) throws Exception {
		final List<InmatesSummary> inmates = agencyLocationService.getInmatesFromLocation(Long.valueOf(locationId), query, orderBy, order, offset, limit);
		InmateSummaries inmateSummaries = new InmateSummaries(inmates, MetaDataFactory.createMetaData(limit, offset, inmates));
		return GetLocationsByLocationIdInmatesResponse.withJsonOK(inmateSummaries);
	}
}
