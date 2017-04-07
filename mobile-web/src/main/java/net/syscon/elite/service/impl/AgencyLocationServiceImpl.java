package net.syscon.elite.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.syscon.elite.persistence.AgencyRepository;
import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.persistence.LocationRepository;
import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.Location;


@Transactional
@Service
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
	public Agency getAgency(final String agencyId) {
		return agencyRepository.find(agencyId);
	}

	@Override
	public List<Agency> getAgencies(final int offset, final int limit) {
		return agencyRepository.findAgencies(offset, limit);
	}

	@Override
	public List<Location> getLocations(final int offset, final int limit) {
		return locationRepository.findLocations(offset, limit);
	}
	
	@Override
	public List<Location> getLocationsFromAgency(final String agencyId, final String query, final int offset, final int limit, final String orderByField, final String order) {
		return locationRepository.findLocationsByAgencyId(agencyId, query, offset, limit, orderByField, order);
	}

	@Override
	public List<AssignedInmate> getInmatesFromLocation(final Long locationId, final int offset, final int limit) {
		return inmateRepository.findInmatesByLocation(locationId, offset, limit);
	}

	@Override
	public Location getLocation(final Long locationId) {
		try {
			final Location location = locationRepository.findLocation(locationId);
			final List<AssignedInmate> inmates = inmateRepository.findInmatesByLocation(locationId, 0, 1000);
			location.setAssignedInmates(inmates);
			return location;
		} catch (final DataAccessException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		}
	}


}
