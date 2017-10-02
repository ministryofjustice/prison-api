package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.persistence.AgencyRepository;
import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.persistence.LocationRepository;
import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.service.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static net.syscon.elite.service.impl.InmateServiceImpl.DEFAULT_OFFENDER_SORT;

@Transactional(readOnly = true)
@Service
public class AgencyLocationServiceImpl implements AgencyLocationService {

	private final AgencyRepository agencyRepository;
	private final LocationRepository locationRepository;
	private final InmateRepository inmateRepository;
	private final UserRepository userRepository;
	private final String locationTypeGranularity;

	@Inject
	public AgencyLocationServiceImpl(AgencyRepository agencyRepository, LocationRepository locationRepository, InmateRepository inmateRepository, UserRepository userRepository,
									 @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity) {
		this.agencyRepository = agencyRepository;
		this.locationRepository = locationRepository;
		this.inmateRepository = inmateRepository;
		this.userRepository = userRepository;
		this.locationTypeGranularity = locationTypeGranularity;
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
	public List<Location> getLocations(String query, String orderBy, Order order, long offset, long limit) {
		return locationRepository.findLocations(query, orderBy, order, offset, limit);
	}
	
	@Override
	public List<Location> getLocationsFromAgency(final String agencyId, final String query, final long offset, final long limit, final String orderByField, final Order order) {
		return locationRepository.findLocationsByAgencyId(getCurrentCaseLoad(), agencyId, query, offset, limit, orderByField, order);
	}

	@Override
	public List<OffenderBooking> getInmatesFromLocation(long locationId, String query, String orderByField, Order order, long offset, long limit) {
		List<OffenderBooking> inmates;

		Location location = getLocation(locationId, false);

		String colSort = StringUtils.isNotBlank(orderByField) ? orderByField : DEFAULT_OFFENDER_SORT;
		if ( location == null) {
			inmates = Collections.emptyList();
		} else {
			inmates = inmateRepository.findInmatesByLocation(locationId, locationTypeGranularity, query, colSort, order, offset, limit);
		}

		return inmates;
	}

	@Override
	public Location getLocation(long locationId, boolean withInmates) {
		Location location = locationRepository.findLocation(locationId).orElseThrow(new EntityNotFoundException(String.valueOf(locationId)));
		if (withInmates) {
			List<OffenderBooking> inmates = inmateRepository.findInmatesByLocation(locationId, locationTypeGranularity, null, null, null, 0, 1000);
			location.setAssignedInmates(inmates);
		}
		return location;
	}

	private String getCurrentCaseLoad() {
		//  get the user data from the database
		final String currentUsername = UserSecurityUtils.getCurrentUsername();
		final UserDetail userDetail = userRepository.findByUsername(currentUsername).orElseThrow(new EntityNotFoundException(currentUsername));
		return userDetail.getActiveCaseLoadId();
	}
}
