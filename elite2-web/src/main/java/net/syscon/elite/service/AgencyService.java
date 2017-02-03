package net.syscon.elite.service;

import java.util.List;

import net.syscon.elite.persistence.domain.AgencyLocation;


public interface AgencyService {
	
	List<AgencyLocation> getLocations(final int offset, final int limit);
	
	
	

}
