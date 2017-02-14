package net.syscon.elite.service;

import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.model.Location;

import java.util.List;


public interface AgencyLocationService {

	Agency getAgency(String agencyId);
	List<Agency> getAgencies(final int offset, final int limit);

	List<Location> getLocationsFromAgency(String agencyId, final int offset, final int limit);

}


