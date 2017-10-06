package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import org.springframework.cache.annotation.Cacheable;
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
	@Cacheable("findByUsername")
	public Optional<UserDetail> findByUsername(final String username) {
		String sql = getQuery("FIND_USER_BY_USERNAME");
		RowMapper<UserDetail> userRowMapper = Row2BeanRowMapper.makeMapping(sql, UserDetail.class, userMapping);

		UserDetail userDetails;
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
	@Cacheable("findByStaffId")
	public Optional<StaffDetail> findByStaffId(Long staffId) {
		String sql = getQuery("FIND_USER_BY_STAFF_ID");
		RowMapper<StaffDetail> staffRowMapper = Row2BeanRowMapper.makeMapping(sql, StaffDetail.class, staffMapping);

		StaffDetail staffDetails;

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
	@Cacheable("findRolesByUsername")
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