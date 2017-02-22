package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateDetail;

import java.util.List;

public interface InmateRepository {

	List<AssignedInmate> findAllInmates(final int offset, final int limit);
	List<AssignedInmate> findInmatesByLocation(Long locationId, final int offset, final int limit);
	InmateDetail findInmate(Long inmateId);

}
