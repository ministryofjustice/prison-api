package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.Agencies;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.model.Locations;
import net.syscon.elite.web.api.resource.AgenciesResource;
import net.syscon.util.MetaDataFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class AgenciesResourceImpl implements AgenciesResource {
	
	private final AgencyLocationService agencyLocationService;

	@Inject
	public AgenciesResourceImpl(final AgencyLocationService agencyLocationService) { this.agencyLocationService = agencyLocationService; }


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
		final List<Location> result = agencyLocationService.getLocationsFromAgency(agencyId, query, offset, limit, orderBy, order.toString());
		Locations locations = new Locations(result, MetaDataFactory.createMetaData(limit, offset, result));
		return GetAgenciesByAgencyIdLocationsResponse.withJsonOK(locations);
	}

}

