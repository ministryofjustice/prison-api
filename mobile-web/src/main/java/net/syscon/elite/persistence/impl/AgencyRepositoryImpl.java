package net.syscon.elite.persistence.impl;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.AgencyRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.util.QueryBuilder;

@Repository
public class AgencyRepositoryImpl extends RepositoryBase implements AgencyRepository {

	private final Map<String, FieldMapper> agencyMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("AGY_LOC_ID", 				new FieldMapper("agencyId"))
		.put("DESCRIPTION", 			new FieldMapper("description"))
		.put("AGENCY_LOCATION_TYPE", 	new FieldMapper("agencyType")
	).build();

	@Override
	public Agency find(final String caseLoadId, final String agencyId) {
		final String sql = getQuery("FIND_AGENCY");
		final RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);
		return jdbcTemplate.queryForObject(sql, createParams("caseLoadId", caseLoadId, "agencyId", agencyId), agencyRowMapper);
	}

	@Override
	public List<Agency> findAgencies(final String caseLoadId, final int offset, final int limit) {
		final String sql =  new QueryBuilder.Builder(getQuery("FIND_ALL_AGENCIES"), agencyMapping, preOracle12)
									.addRowCount()
									.addPagedQuery()
									.build();//getPagedQuery("FIND_ALL_AGENCIES");
		final RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);
		return jdbcTemplate.query(sql, createParams("caseLoadId", caseLoadId, "offset", offset, "limit", limit), agencyRowMapper);
	}
}


