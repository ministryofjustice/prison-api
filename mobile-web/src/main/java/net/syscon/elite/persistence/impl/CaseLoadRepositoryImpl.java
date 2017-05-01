package net.syscon.elite.persistence.impl;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.CaseLoadRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.CaseLoad;

@Repository
public class CaseLoadRepositoryImpl extends RepositoryBase implements CaseLoadRepository {

	private final Map<String, FieldMapper> caseLoadMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("CASELOAD_ID", 			new FieldMapper("caseLoadId"))
		.put("DESCRIPTION", 			new FieldMapper("description")).build();

	@Override
	public CaseLoad find(final Long caseLoadId) {
		final String sql = getQuery("FIND_CASE_LOAD_BY_ID");
		final RowMapper<CaseLoad> caseLoadRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseLoad.class, caseLoadMapping);
		return jdbcTemplate.queryForObject(sql, createParams("caseLoadId", caseLoadId), caseLoadRowMapper);
	}
	
	@Override
	public List<CaseLoad> findCaseLoadsByStaffId(final Long staffId) {
		final String sql = getQuery("FIND_CASE_LOADS_BY_STAFF_ID");
		final RowMapper<CaseLoad> caseLoadRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseLoad.class, caseLoadMapping);
		return jdbcTemplate.query(sql, createParams("staffId", staffId), caseLoadRowMapper);
	}
	

	@Override
	public int updateCurrentLoad(final Long staffId, final String caseLoadId) {
		final String sql = getQuery("UPDATE_STAFF_ACTIVE_CASE_LOAD");
		return jdbcTemplate.update(sql,  createParams());
	}
}


