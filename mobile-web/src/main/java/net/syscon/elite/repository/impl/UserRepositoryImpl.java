package net.syscon.elite.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
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

	private final StandardBeanPropertyRowMapper<UserRole> USER_ROLE_MAPPER =
			new StandardBeanPropertyRowMapper<>(UserRole.class);

	private final StandardBeanPropertyRowMapper<UserDetail> USER_DETAIL_ROW_MAPPER =
			new StandardBeanPropertyRowMapper<>(UserDetail.class);

	@Override
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
	@Cacheable("findRolesByUsername")
	public List<UserRole> findRolesByUsername(final String username, String query) {
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(getQuery("FIND_ROLES_BY_USERNAME"), USER_ROLE_MAPPER);

		if (StringUtils.isNotBlank(query)) {
			builder = builder.addQuery(query);
		}
		String sql = builder
			.addOrderBy(Order.ASC, "roleCode")
			.build();

		return jdbcTemplate.query(sql, createParams("username", username), USER_ROLE_MAPPER);
	}

	@Override
	public void updateWorkingCaseLoad(final Long staffId, final String caseLoadId) {
		final String sql = getQuery("UPDATE_STAFF_ACTIVE_CASE_LOAD");
		jdbcTemplate.update(sql, createParams("caseLoadId", caseLoadId, "staffId", staffId));
	}

	@Override
	public Optional<UserDetail> findByStaffIdAndStaffUserType(Long staffId, String staffUserType) {
		Validate.notNull(staffId, "Staff id is required.");
		Validate.notBlank(staffUserType, "Staff user type is required.");

		String sql = getQuery("FIND_USER_BY_STAFF_ID_STAFF_USER_TYPE");

		UserDetail userDetail;

		try {
			userDetail = jdbcTemplate.queryForObject(
					sql,
					createParams("staffId", staffId, "staffUserType", staffUserType),
					USER_DETAIL_ROW_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			userDetail = null;
		}

		return Optional.ofNullable(userDetail);
	}
}
