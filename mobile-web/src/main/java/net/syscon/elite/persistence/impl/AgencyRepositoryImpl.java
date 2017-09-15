package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.AgencyRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.v2.api.model.Agency;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AgencyRepositoryImpl extends RepositoryBase implements AgencyRepository {

	private final Map<String, FieldMapper> agencyMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("ID", 						new FieldMapper("uid"))
			.put("AGY_LOC_ID", 				new FieldMapper("agencyId"))
			.put("DESCRIPTION", 			new FieldMapper("description"))
			.put("AGENCY_LOCATION_TYPE", 	new FieldMapper("agencyType")).build();

	@Override
	public Optional<Agency> find(String caseLoadId, String agencyId) {
		String sql = getQuery("FIND_AGENCY");
		RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);
		Agency agency;
		try {
			agency = jdbcTemplate.queryForObject(sql, createParams("caseLoadId", caseLoadId, "agencyId", agencyId), agencyRowMapper);
		} catch (EmptyResultDataAccessException e) {
			agency = null;
		}
		return Optional.ofNullable(agency);
	}

	@Override
	public List<Agency> findAgencies(String caseLoadId, int offset, int limit) {
		String sql =  queryBuilderFactory.getQueryBuilder(getQuery("FIND_AGENCIES_BY_CASELOAD"), agencyMapping)
									.addRowCount()
									.addPagination()
									.build();
		RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);
		return jdbcTemplate.query(sql, createParams("caseLoadId", caseLoadId, "offset", offset, "limit", limit), agencyRowMapper);
	}
}
