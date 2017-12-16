package net.syscon.elite.repository.impl;

import net.syscon.util.QueryBuilderFactory;
import net.syscon.util.SQLProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public abstract class RepositoryBase  {
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
