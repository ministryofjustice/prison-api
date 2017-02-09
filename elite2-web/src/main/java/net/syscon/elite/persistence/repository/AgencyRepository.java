package net.syscon.elite.persistence.repository;


import net.syscon.elite.web.api.model.Agency;

import java.util.List;

public interface AgencyRepository {

	List<Agency> findAll(final int offset, final int limit);


}
