package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.CaseLoadRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.CaseLoad;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CaseLoadRepositoryImpl extends RepositoryBase implements CaseLoadRepository {

	private final Map<String, FieldMapper> caseLoadMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("CASELOAD_ID", 			new FieldMapper("caseLoadId"))
		.put("DESCRIPTION", 			new FieldMapper("description")).build();

	@Override
	public Optional<CaseLoad> find(final String caseLoadId) {
		final String sql = getQuery("FIND_CASE_LOAD_BY_ID");
		final RowMapper<CaseLoad> caseLoadRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseLoad.class, caseLoadMapping);

		CaseLoad caseload;
		try {
			caseload = jdbcTemplate.queryForObject(sql, createParams("caseLoadId", caseLoadId), caseLoadRowMapper);
		} catch (EmptyResultDataAccessException e) {
			caseload = null;
		}
		return Optional.ofNullable(caseload);
	}
	
	@Override
	public List<CaseLoad> findCaseLoadsByUsername(final String username) {
		final String sql = getQuery("FIND_CASE_LOADS_BY_STAFF_ID");
		final RowMapper<CaseLoad> caseLoadRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseLoad.class, caseLoadMapping);
		try {
			return jdbcTemplate.query(sql, createParams("username", username), caseLoadRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return Collections.emptyList();
		}
	}
}


