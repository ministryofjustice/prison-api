package net.syscon.elite.persistence;


import java.util.List;

import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateDetails;

public interface InmateRepository {

	List<AssignedInmate> findAllInmates(final int offset, final int limit);
	List<AssignedInmate> findInmatesByLocation(Long locationId, final int offset, final int limit);
	InmateDetails findInmate(Long inmateId);

}
