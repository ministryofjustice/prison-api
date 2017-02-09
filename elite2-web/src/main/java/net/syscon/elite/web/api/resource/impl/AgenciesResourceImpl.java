package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.resource.AgenciesResource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import java.util.List;

@Component
public class AgenciesResourceImpl implements AgenciesResource {
	
	private AgencyLocationService agencyLocationService;

	@Inject
	public void setAgencyLocationService(final AgencyLocationService agencyLocationService) { this.agencyLocationService = agencyLocationService; }


	@Override
	public GetAgenciesResponse getAgencies(final int offset, final int limit) throws Exception {
		return GetAgenciesResponse.withJsonOK(agencyLocationService.getAgencies(offset, limit));
	}

	@Override
	public GetAgenciesAgenciesByAgencyIdResponse getAgenciesAgenciesByAgencyId(String agencyId) throws Exception {
		return GetAgenciesAgenciesByAgencyIdResponse.withJsonOK(agencyLocationService.getAgency(agencyId));
	}

	@Override
	public GetAgenciesAgenciesByAgencyIdLocationsResponse getAgenciesAgenciesByAgencyIdLocations(String agencyId, String orderBy, @DefaultValue("asc") Order order, @DefaultValue("0") int offset, @DefaultValue("10") int limit) throws Exception {
		final List<Location> locations = agencyLocationService.getLocationsFromAgency(agencyId, offset, limit);
		return GetAgenciesAgenciesByAgencyIdLocationsResponse.withJsonOK(locations);
	}

}
