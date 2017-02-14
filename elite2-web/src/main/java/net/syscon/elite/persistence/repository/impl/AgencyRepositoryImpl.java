package net.syscon.elite.persistence.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.repository.AgencyRepository;
import net.syscon.elite.web.api.model.Agency;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AgencyRepositoryImpl extends RepositoryBase implements AgencyRepository {

	private final Map<String, String> agencyMapping = new ImmutableMap.Builder<String, String>()
		.put("ID", "uid")
		.put("AGENCY_ID", "agencyId")
		.put("DESCRIPTION", "description")
		.put("AGENCY_LOCATION_TYPE", "agencyType").build();

	@Override
	public Agency find(String agencyId) {
		String sql = getQuery("FIND_AGENCY");
		RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);
		return jdbcTemplate.queryForObject(sql, createParams("agencyId", agencyId), agencyRowMapper);
	}

	@Override
	public List<Agency> findAgencies(int offset, int limit) {
		String sql = getPagedQuery("FIND_ALL_AGENCIES");
		RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);
		return jdbcTemplate.query(sql, createParams("offset", offset, "limit", limit), agencyRowMapper);
	}
}


