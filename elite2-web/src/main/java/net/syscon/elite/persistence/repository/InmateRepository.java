package net.syscon.elite.persistence.repository;


import net.syscon.elite.web.api.model.AssignedInmate;

import java.util.List;

public interface InmateRepository {

	List<AssignedInmate> findInmatesByLocation(Long locationId, final int offset, final int limit);

}
