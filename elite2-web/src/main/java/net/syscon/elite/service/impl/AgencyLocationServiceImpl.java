package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.AgencyRepository;
import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.persistence.LocationRepository;
import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;


@Transactional
public class AgencyLocationServiceImpl implements AgencyLocationService {


	private final Logger log = LoggerFactory.getLogger(getClass());

	private AgencyRepository agencyRepository;
	private LocationRepository locationRepository;
	private InmateRepository inmateRepository;

	@Inject
	public void setAgencyRepository(final AgencyRepository agencyRepository) { this.agencyRepository = agencyRepository; }

	@Inject
	public void setLocationRepository(final LocationRepository locationRepository) { this.locationRepository = locationRepository; }

	@Inject
	public void setInmateRepository(final InmateRepository inmateRepository) { this.inmateRepository = inmateRepository; }


	@Override
	public Agency getAgency(String agencyId) {
		return agencyRepository.find(agencyId);
	}

	@Override
	public List<Agency> getAgencies(final int offset, final int limit) {
		return agencyRepository.findAgencies(offset, limit);
	}

	@Override
	public List<Location> getLocations(int offset, int limit) {
		return locationRepository.findLocations(offset, limit);
	}

	@Override
	public List<Location> getLocationsFromAgency(String agencyId, int offset, int limit) {
		return locationRepository.findLocationsByAgencyId(agencyId, offset, limit);
	}

	@Override
	public List<AssignedInmate> getInmatesFromLocation(Long locationId, int offset, int limit) {
		return inmateRepository.findInmatesByLocation(locationId, offset, limit);
	}

	@Override
	public Location getLocation(Long locationId) {
		try {
			Location location = locationRepository.findLocation(locationId);
			List<AssignedInmate> inmates = inmateRepository.findInmatesByLocation(locationId, 0, 1000);
			location.setAssignedInmates(inmates);
			return location;
		} catch (Throwable ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		}
	}


}
