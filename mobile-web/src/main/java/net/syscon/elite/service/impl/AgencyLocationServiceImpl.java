package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.AgencyRepository;
import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.persistence.LocationRepository;
import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.security.UserDetailsImpl;
import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.model.UserDetails;
import net.syscon.elite.web.api.resource.LocationsResource.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;


@Transactional
@Service
public class AgencyLocationServiceImpl implements AgencyLocationService {


	private final Logger log = LoggerFactory.getLogger(getClass());

	private AgencyRepository agencyRepository;
	private LocationRepository locationRepository;
	private InmateRepository inmateRepository;
	private UserRepository userRepository;



	@Inject
	public void setAgencyRepository(final AgencyRepository agencyRepository) { this.agencyRepository = agencyRepository; }

	@Inject
	public void setLocationRepository(final LocationRepository locationRepository) { this.locationRepository = locationRepository; }

	@Inject
	public void setInmateRepository(final InmateRepository inmateRepository) { this.inmateRepository = inmateRepository; }

	@Inject
	public void setUserRepository(final UserRepository userRepository) { this.userRepository = userRepository; }


	private String getCurrentCaseLoad() {
		// get the user context from Spring Security
		final UserDetailsImpl currUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//  get the user data from the database
		final UserDetails userDetails = userRepository.findByUsername(currUser.getUsername());
		return userDetails.getActiveCaseLoadId();
	}


	@Override
	public Agency getAgency(final String agencyId) {
		return agencyRepository.find(getCurrentCaseLoad(), agencyId);
	}

	@Override
	public List<Agency> getAgencies(final int offset, final int limit) {
		return agencyRepository.findAgencies(getCurrentCaseLoad(), offset, limit);
	}

	@Override
	public List<Location> getLocations(String query, String orderBy, Order order, final int offset, final int limit) {
		return locationRepository.findLocations(getCurrentCaseLoad(), query, orderBy, order, offset, limit);
	}
	
	@Override
	public List<Location> getLocationsFromAgency(final String agencyId, final String query, final int offset, final int limit, final String orderByField, final String order) {
		return locationRepository.findLocationsByAgencyId(getCurrentCaseLoad(), agencyId, query, offset, limit, orderByField, order);
	}

	@Override
	public List<AssignedInmate> getInmatesFromLocation(final Long locationId, String query, String orderByField, Order order, final int offset, final int limit) {
		return inmateRepository.findInmatesByLocation(locationId, query, orderByField, order, offset, limit);
	}

	@Override
	public Location getLocation(final Long locationId) {
		Location result = null;
		try {
			Location location = locationRepository.findLocation(getCurrentCaseLoad(), locationId);
			if (location != null) {
				final List<AssignedInmate> inmates = inmateRepository.findInmatesByLocation(locationId, null, null, null, 0, 1000);
				location.setAssignedInmates(inmates);
				return location;
			}
		} catch (final DataAccessException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		}
		return result;
	}


}
