package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.StaffUserRole;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl extends RepositoryBase implements UserRepository {

	private final StandardBeanPropertyRowMapper<UserRole> USER_ROLE_MAPPER =
			new StandardBeanPropertyRowMapper<>(UserRole.class);

    private final StandardBeanPropertyRowMapper<StaffUserRole> STAFF_USER_ROLE_MAPPER =
            new StandardBeanPropertyRowMapper<>(StaffUserRole.class);

	private final StandardBeanPropertyRowMapper<UserDetail> USER_DETAIL_ROW_MAPPER =
			new StandardBeanPropertyRowMapper<>(UserDetail.class);

	@Override
	public Optional<UserDetail> findByUsername(final String username) {
		String sql = getQuery("FIND_USER_BY_USERNAME");
		UserDetail userDetails;
		try {
			userDetails = jdbcTemplate.queryForObject(
					sql,
					createParams("username", username),
                    USER_DETAIL_ROW_MAPPER);
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
    @Cacheable("findByStaffIdAndStaffUserType")
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

	@Override
	public boolean isUserAssessibleCaseloadAvailable(String caseload, String username) {
		Validate.notBlank(caseload, "caseload is required.");
		Validate.notBlank(username, "username is required.");

		Long count = jdbcTemplate.queryForObject(
				getQuery("USER_ACCESSIBLE_CASELOAD_COUNT"),
				createParams("caseloadId", caseload, "username", username),
				Long.class);

		return count != null && count > 0;
	}

    @Override
    public Optional<Long> getRoleIdForCode(String roleCode) {
	    Validate.notBlank(roleCode, "roleCode is required.");

	    Long roleId;
        try {
            roleId = jdbcTemplate.queryForObject(
                    getQuery("GET_ROLE_ID_FOR_ROLE_CODE"),
                    createParams("roleCode", roleCode),
                    Long.class);

        } catch (final EmptyResultDataAccessException ex) {
            roleId = null;
       }
		return Optional.ofNullable(roleId);
    }



	@Override
	public void addUserAssessibleCaseload(String caseload, String username) {
		Validate.notBlank(caseload, "caseload is required.");
		Validate.notBlank(username, "username is required.");

		jdbcTemplate.update(
				getQuery("USER_ACCESSIBLE_CASELOAD_INSERT"),
				createParams("caseloadId", caseload, "username", username));
	}

	@Override
	public List<StaffUserRole> getAllStaffRolesForCaseload(String caseload, String roleCode) {
		Validate.notBlank(caseload, "caseload is required.");
		Validate.notBlank(roleCode, "roleCode is required.");

        return jdbcTemplate.query(getQuery("FIND_ROLES_BY_CASELOAD_AND_ROLE"),
                createParams("caseloadId", caseload, "roleCode", roleCode),
                STAFF_USER_ROLE_MAPPER);

	}

	@Override
    @CacheEvict(value="findRolesByUsername", key="")
    public void addRole(String username, String caseload, Long roleId) {
		Validate.notBlank(caseload, "caseload is required.");
		Validate.notBlank(username, "username is required.");
		Validate.notNull(roleId, "roleId is required.");

        jdbcTemplate.update(
                getQuery("INSERT_USER_ROLE"),
                createParams("caseloadId", caseload, "username", username, "roleId", roleId));
	}

	@Override
    @CacheEvict(value="findRolesByUsername", key="")
	public void removeRole(String username, String caseload, Long roleId) {
		Validate.notBlank(caseload, "caseload is required.");
		Validate.notBlank(username, "username is required.");
		Validate.notNull(roleId, "roleId is required.");

        jdbcTemplate.update(
                getQuery("DELETE_USER_ROLE"),
                createParams("caseloadId", caseload, "username", username, "roleId", roleId));
	}

}
