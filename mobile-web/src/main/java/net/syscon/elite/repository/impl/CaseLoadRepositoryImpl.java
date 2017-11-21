package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.repository.CaseLoadRepository;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class CaseLoadRepositoryImpl extends RepositoryBase implements CaseLoadRepository {
	private static final RowMapper<CaseLoad> CASELOAD_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(CaseLoad.class);

	public Set<String> getUserCaseloadIds(String username) {
		return findCaseLoadsByUsername(username).stream().map(CaseLoad::getCaseLoadId).collect(Collectors.toSet());
	}

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
	@Cacheable("findCaseLoadsByUsername")
	public List<CaseLoad> findCaseLoadsByUsername(final String username) {
		final String sql = getQuery("FIND_CASE_LOADS_BY_USERNAME");
		final RowMapper<CaseLoad> caseLoadRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseLoad.class, caseLoadMapping);
		try {
			return jdbcTemplate.query(sql, createParams("username", username), caseLoadRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return Collections.emptyList();
		}
	}

	public Optional<CaseLoad> getCurrentCaseLoadDetail(final String username) {
		String sql = getQuery("FIND_ACTIVE_CASE_LOAD_BY_USERNAME");

//		RowMapper<CaseLoad> caseLoadRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseLoad.class, caseLoadMapping);

		CaseLoad caseload;

		try {
			caseload = jdbcTemplate.queryForObject(
					sql,
					createParams("username", username),
					CASELOAD_ROW_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			caseload = null;
		}

		return Optional.ofNullable(caseload);
	}
}
