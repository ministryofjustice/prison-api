package net.syscon.elite.persistence.impl;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import net.syscon.util.SQLProvider;


public class RepositoryBase implements ApplicationContextAware {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected NamedParameterJdbcOperations jdbcTemplate;
	protected SQLProvider sqlProvider;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		jdbcTemplate = applicationContext.getBean(NamedParameterJdbcOperations.class);
		final String resourcePath = "classpath:sqls/" + getClass().getSimpleName().replace('.', '/') + ".sql";
		final Resource resource = applicationContext.getResource(resourcePath);
		this.sqlProvider = new SQLProvider();
		try {
			sqlProvider.loadFromStream(resource.getInputStream());
		} catch (final IOException ex) {
			log.error(ex.getMessage(), ex);
		}
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

	public String getPagedQuery(final String name) {
		final StringBuilder sb = new StringBuilder(sqlProvider.get(name));
		if (sb.length() > 0) {
			sb.append(" OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY");
		}
		return sb.toString();
	}

	public String getQuery(final String name) {
		return sqlProvider.get(name);
	}


}
