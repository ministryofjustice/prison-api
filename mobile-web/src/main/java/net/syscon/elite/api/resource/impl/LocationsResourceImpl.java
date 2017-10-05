package net.syscon.elite.api.resource.impl;


import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.resource.LocationResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AgencyLocationService;
import net.syscon.util.MetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Path;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;


@RestResource
@Path("/locations")
public class LocationsResourceImpl implements LocationResource {
	@Autowired
	private AgencyLocationService agencyLocationService;

	@Override
	public GetLocationsResponse getLocations(String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		final List<Location> locationsResult = agencyLocationService.getLocations(query, sortFields, sortOrder, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
		return GetLocationsResponse.respond200WithApplicationJson(locationsResult, MetaDataFactory.getTotalRecords(locationsResult), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
	}

	@Override
	public GetLocationResponse getLocation(final Long locationId) {
		return GetLocationResponse.respond200WithApplicationJson(agencyLocationService.getLocation(locationId, false));
	}

	@Override
	public GetOffendersAtLocationResponse getOffendersAtLocation(Long locationId, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		final List<OffenderBooking> inmates = agencyLocationService.getInmatesFromLocation(locationId, query, sortFields, sortOrder, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
		return GetOffendersAtLocationResponse.respond200WithApplicationJson(inmates, MetaDataFactory.getTotalRecords(inmates), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
	}
}
