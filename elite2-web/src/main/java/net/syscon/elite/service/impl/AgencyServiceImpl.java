package net.syscon.elite.service.impl;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.jvnet.hk2.annotations.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import net.syscon.elite.persistence.domain.AgencyLocation;
import net.syscon.elite.persistence.repository.AgencyLocationRepository;
import net.syscon.elite.service.AgencyService;


@Transactional
@Service(name="agencyService")
public class AgencyServiceImpl implements AgencyService {
	
	private AgencyLocationRepository agencyLocationRepository;
	
	@Inject
	public void setAgencyLocationRepository(final AgencyLocationRepository agencyLocationRepository) {
		this.agencyLocationRepository = agencyLocationRepository;
	}
	
	@Override
	public List<AgencyLocation> getLocations(final int offset, final int limit) {
		final int pageNumber = offset / limit;
		final PageRequest pageRequest = new PageRequest(pageNumber, limit);
		final Page<AgencyLocation> page = agencyLocationRepository.findAll(pageRequest);
		return page.getContent();
	}
	
	

}
