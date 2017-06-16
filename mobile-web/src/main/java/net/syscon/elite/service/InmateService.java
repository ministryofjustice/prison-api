package net.syscon.elite.service;


import net.syscon.elite.web.api.model.Alias;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateAssignmentSummary;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.resource.BookingResource;
import net.syscon.elite.web.api.resource.LocationsResource;

import java.util.List;


public interface InmateService {

	List<AssignedInmate> findAllInmates(final String query, final int offset, final int limit, final String orderBy, BookingResource.Order order);
	List<AssignedInmate> findInmatesByLocation(Long locationId, String query, String orderByField, LocationsResource.Order order, final int offset, final int limit);
	InmateDetails findInmate(Long inmateId);
	List<Alias> findInmateAliases(final long inmateId, String orderByField, BookingResource.Order order, final int offset, final int limit);
	List<InmateAssignmentSummary> findMyAssignments(long staffId, String currentCaseLoad, int offset, int limit);
}
