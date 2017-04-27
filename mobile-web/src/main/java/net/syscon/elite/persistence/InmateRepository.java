package net.syscon.elite.persistence;


import java.util.List;

import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.resource.BookingResource.Order;


public interface InmateRepository {

	List<AssignedInmate> findAllInmates(final String query, final int offset, final int limit, final String orderBy, Order order);
	List<AssignedInmate> findInmatesByLocation(Long locationId, final int offset, final int limit);
	InmateDetails findInmate(Long inmateId);

}
