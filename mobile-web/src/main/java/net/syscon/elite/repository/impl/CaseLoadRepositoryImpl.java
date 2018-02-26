package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.repository.CaseLoadRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CaseLoadRepositoryImpl extends RepositoryBase implements CaseLoadRepository {

	private static final StandardBeanPropertyRowMapper<CaseLoad> CASELOAD_ROW_MAPPER =
			new StandardBeanPropertyRowMapper<>(CaseLoad.class);


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
	public List<CaseLoad> getCaseLoadsByUsername(String username, String query) {

		String initialSql = getQuery("FIND_CASE_LOADS_BY_USERNAME");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, CASELOAD_ROW_MAPPER);

		if (StringUtils.isNotBlank(query)) {
			builder = builder.addQuery(query);
		}
		String sql = builder.build();

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
