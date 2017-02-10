package net.syscon.elite.persistence.repository.impl;

import net.syscon.elite.persistence.mapping.AgencyMapping;
import net.syscon.elite.persistence.repository.AgencyRepository;
import net.syscon.elite.web.api.model.Agency;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AgencyRepositoryImpl extends RepositoryBase implements AgencyRepository {

	private final AgencyMapping agencyMapping = new AgencyMapping();

	@Override
	public Agency find(String agencyId) {
		String sql = getPagedQuery("FIND_AGENCY");
		return jdbcTemplate.queryForObject(sql, createParams("agencyId", agencyId), agencyMapping);
	}

	@Override
	public List<Agency> findAll(int offset, int limit) {
		String sql = getPagedQuery("FIND_ALL_AGENCIES");
		return jdbcTemplate.query(sql, createParams("offset", offset, "limit", limit), agencyMapping);
	}
}


