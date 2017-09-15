package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.AgencyRepository;
import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.persistence.LocationRepository;
import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.v2.api.model.Agency;
import net.syscon.elite.web.api.model.InmatesSummary;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.model.UserDetails;
import net.syscon.elite.web.api.resource.LocationsResource.Order;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static net.syscon.elite.service.impl.InmateServiceImpl.DEFAULT_OFFENDER_SORT;

@Transactional(readOnly = true)
@Service
public class AgencyLocationServiceImpl implements AgencyLocationService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final AgencyRepository agencyRepository;
	private final LocationRepository locationRepository;
	private final InmateRepository inmateRepository;
	private final UserRepository userRepository;

	@Inject
	public AgencyLocationServiceImpl(AgencyRepository agencyRepository, LocationRepository locationRepository, InmateRepository inmateRepository, UserRepository userRepository) {
		this.agencyRepository = agencyRepository;
		this.locationRepository = locationRepository;
		this.inmateRepository = inmateRepository;
		this.userRepository = userRepository;
	}

    @Override
	public Agency getAgency(final String agencyId) {
		return agencyRepository.find(getCurrentCaseLoad(), agencyId).orElseThrow(new EntityNotFoundException(agencyId));
	}

	@Override
	public List<Agency> getAgencies(final int offset, final int limit) {
		return agencyRepository.findAgencies(getCurrentCaseLoad(), offset, limit);
	}

	@Override
	public List<Location> getLocations(String query, String orderBy, Order order, int offset, int limit) {
		return locationRepository.findLocations(query, orderBy, order, offset, limit);
	}
	
	@Override
	public List<Location> getLocationsFromAgency(final String agencyId, final String query, final int offset, final int limit, final String orderByField, final Order order) {
		return locationRepository.findLocationsByAgencyId(getCurrentCaseLoad(), agencyId, query, offset, limit, orderByField, order);
	}

	@Override
	public List<InmatesSummary> getInmatesFromLocation(Long locationId, String query, String orderByField, Order order, int offset, int limit) {
		List<InmatesSummary> inmates;

		Location location = getLocation(locationId, false);

		String colSort = StringUtils.isNotBlank(orderByField) ? orderByField : DEFAULT_OFFENDER_SORT;
		if ( location == null) {
			inmates = Collections.emptyList();
		} else {
			inmates = inmateRepository.findInmatesByLocation(locationId, query, colSort, order, offset, limit);
		}

		return inmates;
	}

	@Override
	public Location getLocation(Long locationId, boolean withInmates) {
		Location location = locationRepository.findLocation(locationId).orElseThrow(new EntityNotFoundException(String.valueOf(locationId)));
		if (withInmates) {
			List<InmatesSummary> inmates = inmateRepository.findInmatesByLocation(locationId, null, null, null, 0, 1000);
			location.setAssignedInmates(inmates);
		}
		return location;
	}

	private String getCurrentCaseLoad() {
		//  get the user data from the database
		final String currentUsername = UserSecurityUtils.getCurrentUsername();
		final UserDetails userDetails = userRepository.findByUsername(currentUsername).orElseThrow(new EntityNotFoundException(currentUsername));
		return userDetails.getActiveCaseLoadId();
	}
}
