package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.Agency;
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
		final List<Agency> result = agencyLocationService.getAgencies(offset, limit);
		return GetAgenciesResponse.withJsonOK(result);
	}

	@Override
	public GetAgenciesAgenciesByAgencyIdResponse getAgenciesAgenciesByAgencyId(String agencyId) throws Exception {
		final Agency result = agencyLocationService.getAgency(agencyId);
		return GetAgenciesAgenciesByAgencyIdResponse.withJsonOK(result);
	}

	@Override
	public GetAgenciesAgenciesByAgencyIdLocationsResponse getAgenciesAgenciesByAgencyIdLocations(String agencyId, String orderBy, @DefaultValue("asc") Order order, @DefaultValue("0") int offset, @DefaultValue("10") int limit) throws Exception {
		final List<Location> result = agencyLocationService.getLocationsFromAgency(agencyId, offset, limit);
		return GetAgenciesAgenciesByAgencyIdLocationsResponse.withJsonOK(result);
	}

}
