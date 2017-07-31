package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.StaffDetails;
import net.syscon.elite.web.api.model.UserDetails;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepositoryImpl extends RepositoryBase implements UserRepository {

	private final Map<String, FieldMapper> userMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("STAFF_ID", new FieldMapper("staffId"))
		.put("USER_ID", new FieldMapper("username"))
		.put("FIRST_NAME", new FieldMapper("firstName"))
		.put("LAST_NAME", new FieldMapper("lastName"))
		.put("EMAIL", new FieldMapper("email"))
		.put("IMAGE_ID", new FieldMapper("thumbnailId"))
		.put("ASSIGNED_CASELOAD_ID", new FieldMapper("activeCaseLoadId")
	).build();

	private final Map<String, FieldMapper> staffMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("STAFF_ID", new FieldMapper("staffId"))
			.put("FIRST_NAME", new FieldMapper("firstName"))
			.put("LAST_NAME", new FieldMapper("lastName"))
			.put("EMAIL", new FieldMapper("email"))
			.put("IMAGE_ID", new FieldMapper("thumbnailId")).build();


	@Override
	public Optional<UserDetails> findByUsername(final String username) {
		String sql = getQuery("FIND_USER_BY_USERNAME");
		RowMapper<UserDetails> userRowMapper = Row2BeanRowMapper.makeMapping(sql, UserDetails.class, userMapping);

		UserDetails userDetails;
		try {
			userDetails = jdbcTemplate.queryForObject(
					sql,
					createParams("username", username),
					userRowMapper);
		} catch (final EmptyResultDataAccessException ex) {
			userDetails = null;
		}
		return Optional.ofNullable(userDetails);
	}

	@Override
	public Optional<StaffDetails> findByStaffId(Long staffId) {
		String sql = getQuery("FIND_USER_BY_STAFF_ID");
		RowMapper<StaffDetails> staffRowMapper = Row2BeanRowMapper.makeMapping(sql, StaffDetails.class, staffMapping);

		StaffDetails staffDetails;

		try {
			staffDetails = jdbcTemplate.queryForObject(
					sql,
					createParams("staffId", staffId),
					staffRowMapper);
		} catch (EmptyResultDataAccessException ex) {
			staffDetails = null;
		}

		return Optional.ofNullable(staffDetails);
	}

	@Override
	public List<String> findRolesByUsername(final String username) {
		final String sql = getQuery("FIND_ROLES_BY_USERNAME");
		return jdbcTemplate.queryForList(sql, createParams("username", username), String.class);
	}


	@Override
	public void updateCurrentLoad(final Long staffId, final String caseLoadId) {
		final String sql = getQuery("UPDATE_STAFF_ACTIVE_CASE_LOAD");
		jdbcTemplate.update(sql, createParams("caseLoadId", caseLoadId, "staffId", staffId));
	}

}