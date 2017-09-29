package net.syscon.elite.persistence;



import net.syscon.elite.api.model.Agency;

import java.util.List;
import java.util.Optional;

public interface AgencyRepository {

	Optional<Agency> find(final String caseLoadId, String agencyId);
	List<Agency> findAgencies(final String caseLoadId, final int offset, final int limit);

}

