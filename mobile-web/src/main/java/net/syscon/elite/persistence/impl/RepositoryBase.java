package net.syscon.elite.persistence.impl;


import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.v2.api.model.CaseLoad;
import net.syscon.util.QueryBuilderFactory;
import net.syscon.util.SQLProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

@Component
public abstract class RepositoryBase  {

	protected final Map<String, FieldMapper> caseLoadMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("CASELOAD_ID", 			new FieldMapper("caseLoadId"))
			.put("DESCRIPTION", 			new FieldMapper("description"))
			.put("CASELOAD_TYPE", 			new FieldMapper("type")).build();

	@Autowired
	protected NamedParameterJdbcOperations jdbcTemplate;

	@Autowired
	protected SQLProvider sqlProvider;

	@Autowired
	protected QueryBuilderFactory queryBuilderFactory;

	@PostConstruct
	public void initSql() {
		sqlProvider.loadSql(getClass().getSimpleName().replace('.', '/'));
	}

	@Transactional(readOnly = true)
	protected String getCurrentCaseLoad() {
		final String sql = getQuery("GET_CASELOAD_ID");
		try {
			return jdbcTemplate.queryForObject(sql, createParams("username", UserSecurityUtils.getCurrentUsername()), String.class);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public Optional<CaseLoad> getCurrentCaseLoadDetail() {
		final String sql = getQuery("FIND_ACTIVE_CASE_LOAD_BY_USERNAME");
		final RowMapper<CaseLoad> caseLoadRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseLoad.class, caseLoadMapping);

		CaseLoad caseload;
		try {
			caseload = jdbcTemplate.queryForObject(sql, createParams("username", UserSecurityUtils.getCurrentUsername()), caseLoadRowMapper);
		} catch (EmptyResultDataAccessException e) {
			caseload = null;
		}
		return Optional.ofNullable(caseload);
	}

	public MapSqlParameterSource createParams(final Object ... keysValues) {
		if (keysValues.length %2 != 0) throw new IllegalArgumentException("The keysValues must always be in pairs");
		final MapSqlParameterSource params = new MapSqlParameterSource();
		for (int i = 0; i < keysValues.length / 2; i++) {
			final int j = i * 2;
			params.addValue(keysValues[j].toString(), keysValues[j + 1]);
		}
		return params;
	}


	public String getQuery(final String name) {
		return sqlProvider.get(name);
	}


}
