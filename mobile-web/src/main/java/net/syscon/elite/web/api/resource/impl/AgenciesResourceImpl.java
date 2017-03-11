package net.syscon.elite.web.api.resource.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.resource.AgenciesResource;

@Component
public class AgenciesResourceImpl implements AgenciesResource {
	
	private AgencyLocationService agencyLocationService;

	@Inject
	public void setAgencyLocationService(final AgencyLocationService agencyLocationService) { this.agencyLocationService = agencyLocationService; }


	@Override
	public GetAgenciesResponse getAgencies(final int offset, final int limit) throws Exception {
		try {
			final List<Agency> agencies = agencyLocationService.getAgencies(offset, limit);
			return GetAgenciesResponse.withJsonOK(agencies);
		} catch (final Throwable ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	@Override
	public GetAgenciesByAgencyIdResponse getAgenciesByAgencyId(final String agencyId) throws Exception {
		final Agency agency = agencyLocationService.getAgency(agencyId);
		return GetAgenciesByAgencyIdResponse.withJsonOK(agency);
	}

	@Override
	public GetAgenciesByAgencyIdLocationsResponse getAgenciesByAgencyIdLocations(final String agencyId, final String orderBy, final Order order, final int offset, final int limit) throws Exception {
		final List<Location> result = agencyLocationService.getLocationsFromAgency(agencyId, offset, limit);
		return GetAgenciesByAgencyIdLocationsResponse.withJsonOK(result);
	}

}

