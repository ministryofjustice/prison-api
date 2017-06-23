package net.syscon.elite.service;


import net.syscon.elite.web.api.model.Alias;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateAssignmentSummary;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.resource.BookingResource;
import net.syscon.elite.web.api.resource.LocationsResource;

import java.util.List;


public interface InmateService {

	List<AssignedInmate> findAllInmates(String query, int offset, int limit, String orderBy, BookingResource.Order order);
	List<AssignedInmate> findInmatesByLocation(Long locationId, String query, String orderByField, LocationsResource.Order order, int offset, int limit);
	InmateDetails findInmate(Long inmateId);
	List<Alias> findInmateAliases(Long inmateId, String orderByField, BookingResource.Order order);
	List<InmateAssignmentSummary> findMyAssignments(long staffId, String currentCaseLoad, int offset, int limit);
}
