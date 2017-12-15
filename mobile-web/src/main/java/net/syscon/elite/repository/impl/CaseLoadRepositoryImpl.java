package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.repository.CaseLoadRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class CaseLoadRepositoryImpl extends RepositoryBase implements CaseLoadRepository {
	private static final RowMapper<CaseLoad> CASELOAD_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(CaseLoad.class);

	@Override
	public Set<String> getCaseLoadIdsByUsername(String username) {
		return getCaseLoadsByUsername(username).stream().map(CaseLoad::getCaseLoadId).collect(Collectors.toSet());
	}

	@Override
	public Optional<CaseLoad> getCaseLoad(final String caseLoadId) {
		String sql = getQuery("FIND_CASE_LOAD_BY_ID");

		CaseLoad caseload;

		try {
			caseload = jdbcTemplate.queryForObject(
					sql,
					createParams("caseLoadId", caseLoadId),
					CASELOAD_ROW_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			caseload = null;
		}

		return Optional.ofNullable(caseload);
	}
	
	@Override
	@Cacheable("getCaseLoadsByUsername")
	public List<CaseLoad> getCaseLoadsByUsername(String username) {
		String sql = getQuery("FIND_CASE_LOADS_BY_USERNAME");

		return jdbcTemplate.query(
				sql,
				createParams("username", username),
				CASELOAD_ROW_MAPPER);
	}

	@Override
	public Optional<CaseLoad> getWorkingCaseLoadByUsername(final String username) {
		String sql = getQuery("FIND_ACTIVE_CASE_LOAD_BY_USERNAME");

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
