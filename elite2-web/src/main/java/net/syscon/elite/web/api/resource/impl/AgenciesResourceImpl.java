package net.syscon.elite.web.api.resource.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import net.syscon.elite.service.AgencyService;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.resource.AgenciesResource;

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
