package net.syscon.elite.persistence.repository;


import net.syscon.elite.web.api.model.Agency;

import java.util.List;

public interface AgencyRepository {

	Agency find(String agencyId);
	List<Agency> findAgencies(final int offset, final int limit);

}

