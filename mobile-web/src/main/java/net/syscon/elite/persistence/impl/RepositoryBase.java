package net.syscon.elite.persistence.impl;


import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.util.QueryBuilderFactory;
import net.syscon.util.SQLProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.io.IOException;


public class RepositoryBase implements ApplicationContextAware {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected NamedParameterJdbcOperations jdbcTemplate;
	protected SQLProvider sqlProvider;

	@Value("${schema.type}")
	protected String schemaType;

	@Value("${schema.pre.oracle12:false}")
	protected boolean preOracle12;

	@Autowired
	protected QueryBuilderFactory queryBuilderFactory;

	// TODO: Remove UserRepository dependency using SQLFilter approach to generate the filter
	//************************** PLEASE, FIX ME LATER!!! **************************
	protected String getCurrentCaseLoad() {
		final String username = UserSecurityUtils.getCurrentUsername();

		String sql;

		// TODO: temp hack for nomis - use the SQLFilter approach instead
		if (!schemaType.equalsIgnoreCase("nomis")) {
			sql = "SELECT ASSIGNED_CASELOAD_ID FROM STAFF_MEMBERS WHERE PERSONNEL_TYPE = 'STAFF' AND USER_ID = :username";
		} else {
			sql = "SELECT WORKING_CASELOAD_ID FROM STAFF_USER_ACCOUNTS WHERE USERNAME = :username";
		}

		try {
			return jdbcTemplate.queryForObject(sql, createParams("username", username), String.class);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	//********************************************************************************



	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		jdbcTemplate = applicationContext.getBean(NamedParameterJdbcOperations.class);
		this.sqlProvider = new SQLProvider();

		loadSql(applicationContext, "classpath:sqls/" + getClass().getSimpleName().replace('.', '/') + ".sql");
		if (StringUtils.isNotBlank(schemaType)) {
			loadSql(applicationContext, "classpath:sqls/" + schemaType + "/"  + getClass().getSimpleName().replace('.', '/') + ".sql");
		}
	}

	private void loadSql(ApplicationContext applicationContext, String resourcePath) {
		final Resource resource = applicationContext.getResource(resourcePath);
		if (resource.exists()) {
			try {
				sqlProvider.loadFromStream(resource.getInputStream());
			} catch (final IOException ex) {
				log.error(ex.getMessage(), ex);
			}
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


	public String getQuery(final String name) {
		return sqlProvider.get(name);
	}


}
