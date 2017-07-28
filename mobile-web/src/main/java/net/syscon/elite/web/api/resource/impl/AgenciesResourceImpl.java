package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.Agencies;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.model.Locations;
import net.syscon.elite.web.api.resource.AgenciesResource;
import net.syscon.util.MetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Path;
import java.util.List;

import static net.syscon.elite.web.api.resource.LocationsResource.Order.asc;
import static net.syscon.elite.web.api.resource.LocationsResource.Order.desc;

@RestResource
@Path("/agencies")
public class AgenciesResourceImpl implements AgenciesResource {
	@Autowired
	private AgencyLocationService agencyLocationService;

	@Override
	public GetAgenciesResponse getAgencies(final int offset, final int limit) throws Exception {
		final List<Agency> agencyItems = agencyLocationService.getAgencies(offset, limit);
		final Agencies agencies = new Agencies();
		agencies.setPageMetaData(MetaDataFactory.createMetaData(limit, offset, agencyItems));
		agencies.setAgencies(agencyItems);
		return GetAgenciesResponse.withJsonOK(agencies);
	}

	@Override
	public GetAgenciesByAgencyIdResponse getAgenciesByAgencyId(final String agencyId) throws Exception {
		final Agency agency = agencyLocationService.getAgency(agencyId);
		return GetAgenciesByAgencyIdResponse.withJsonOK(agency);
	}

	@Override
	public GetAgenciesByAgencyIdLocationsResponse getAgenciesByAgencyIdLocations(final String agencyId, final String query, final String orderBy, final Order order, final int offset, final int limit) throws Exception {
		final List<Location> result = agencyLocationService.getLocationsFromAgency(agencyId, query, offset, limit, orderBy, order == Order.asc ? asc : desc);
		Locations locations = new Locations(result, MetaDataFactory.createMetaData(limit, offset, result));
		return GetAgenciesByAgencyIdLocationsResponse.withJsonOK(locations);
	}
}
