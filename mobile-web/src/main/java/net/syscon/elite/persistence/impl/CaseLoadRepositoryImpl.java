package net.syscon.elite.persistence.impl;

import net.syscon.elite.persistence.CaseLoadRepository;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.v2.api.model.CaseLoad;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class CaseLoadRepositoryImpl extends RepositoryBase implements CaseLoadRepository {

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
		final String sql = getQuery("FIND_CASE_LOADS_BY_USERNAME");
		final RowMapper<CaseLoad> caseLoadRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseLoad.class, caseLoadMapping);
		try {
			return jdbcTemplate.query(sql, createParams("username", username), caseLoadRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return Collections.emptyList();
		}
	}
}


