package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.service.AgencyService;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.resource.AgenciesResource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AgenciesResourceImpl implements AgenciesResource {
	
	private AgencyService agencyService;

	@Inject
	public void setAgencyService(final AgencyService agencyService) { this.agencyService = agencyService; }

	
	@Override
	public GetAgenciesResponse getAgencies(final int offset, final int limit) throws Exception {

		final List<Agency> result = agencyService.getLocations(offset, limit)
				.stream().map(agencyLocation -> {
					final Agency agency = new Agency();
					BeanUtils.copyProperties(agencyLocation, agency);
					return agency;
				}).collect(Collectors.toList());
		
		return GetAgenciesResponse.withJsonOK(result);
	}
	
	
	

}
