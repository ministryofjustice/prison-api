package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.Agency;

import java.util.List;

public interface AgencyRepository {

	Agency find(final String caseLoadId, String agencyId);
	List<Agency> findAgencies(final String caseLoadId, final int offset, final int limit);

}

