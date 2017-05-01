package net.syscon.elite.persistence.impl;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.UserDetails;
import net.syscon.util.SQLProvider;

@Repository
public class UserRepositoryImpl extends RepositoryBase implements UserRepository {
	
	public UserRepositoryImpl() {
	}
	
	public UserRepositoryImpl(final DataSource dataSource) {
		final String filename = "sqls/" + UserRepositoryImpl.class.getSimpleName() + ".sql";
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		this.sqlProvider = new SQLProvider() ;
		this.sqlProvider.loadFromClassLoader(filename);
	}

	private final Map<String, FieldMapper> userMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("STAFF_ID", new FieldMapper("staffId"))
		.put("USER_ID", new FieldMapper("username"))
		.put("FIRST_NAME", new FieldMapper("firstName"))
		.put("LAST_NAME", new FieldMapper("lastName"))
		.put("EMAIL", new FieldMapper("email"))
		.put("IMAGE_ID", new FieldMapper("imageId"))
		.put("ASSIGNED_CASELOAD_ID", new FieldMapper("activeCaseLoadId")
	).build();

	@Override
	public UserDetails findByUsername(final String username) {
		final String sql = getQuery("FIND_USER_BY_USERNAME");
		final RowMapper<UserDetails> userRowMapper = Row2BeanRowMapper.makeMapping(sql, UserDetails.class, userMapping);
		try {
			final UserDetails userDetails = jdbcTemplate.queryForObject(sql, createParams("username", username), userRowMapper);
			return userDetails;
		} catch (final DataAccessException ex) {
			throw new EliteRuntimeException(ex.getMessage(), ex);
		}
	}

	@Override
	public UserDetails findByStaffId(final Long staffId) {
		final String sql = getQuery("FIND_USER_BY_USERNAME");
		final RowMapper<UserDetails> userRowMapper = Row2BeanRowMapper.makeMapping(sql, UserDetails.class, userMapping);
		try {
			final UserDetails userDetails = jdbcTemplate.queryForObject(sql,  createParams("staffId", staffId), userRowMapper);
			return userDetails;
		} catch (final DataAccessException ex) {
			throw new EliteRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@Override
	public List<String> findRolesByUsername(final String username) {
		final String sql = getQuery("FIND_ROLES_BY_USERNAME");
		final List<String> result = jdbcTemplate.queryForList(sql, createParams("username", username), String.class);
		return result;
	}


	@Override
	public int updateCurrentLoad(final Long staffId, final String caseLoadId) {
		final String sql = getQuery("UPDATE_STAFF_ACTIVE_CASE_LOAD");
		return jdbcTemplate.update(sql,  createParams());
	}

}