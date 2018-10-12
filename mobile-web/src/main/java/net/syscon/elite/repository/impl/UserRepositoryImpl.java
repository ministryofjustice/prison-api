package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.StaffUserRole;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl extends RepositoryBase implements UserRepository {

	@Value("${application.caseload.id:NWEB}")
	private String apiCaseloadId;

	@Value("${application.type:APP}")
	private String applicationType;

	private static final String NAME_FILTER_QUERY_TEMPLATE = " AND (UPPER(FIRST_NAME) LIKE :nameFilter OR UPPER(LAST_NAME) LIKE :nameFilter OR UPPER(SUA.USERNAME) LIKE :nameFilter)";

	private static final String APPLICATION_ROLE_CODE_FILTER_QUERY_TEMPLATE = " AND SUA.username in  (select SUA_INNER.USERNAME FROM STAFF_USER_ACCOUNTS SUA_INNER\n" +
            "                INNER JOIN USER_ACCESSIBLE_CASELOADS UAC ON SUA_INNER.USERNAME = UAC.USERNAME\n" +
            "                INNER JOIN User_caseload_roles UCR ON UCR.USERNAME = SUA_INNER.username\n" +
            "                INNER JOIN OMS_ROLES RL ON RL.ROLE_ID = UCR.ROLE_ID\n" +
            "  WHERE UAC.CASELOAD_ID = :apiCaseloadId\n" +
            "  AND RL.ROLE_TYPE =  :applicationType\n" +
            "  AND RL.ROLE_CODE = :roleCode )";

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
	public List<UserRole> findAccessRolesByUsernameAndCaseload(final String username, String caseload) {
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(getQuery("FIND_ACCESS_ROLES_BY_USERNAME_AND_CASELOAD"), USER_ROLE_MAPPER);

		String sql = builder
				.addOrderBy(Order.ASC, "roleName")
				.build();

		return jdbcTemplate.query(sql, createParams("username", username, "caseloadId", caseload), USER_ROLE_MAPPER);
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
	public boolean isRoleAssigned(String username, String caseload, long roleId) {
		Validate.notBlank(caseload, "caseload is required.");
		Validate.notBlank(username, "username is required.");

		Long count = jdbcTemplate.queryForObject(
				getQuery("ROLE_ASSIGNED_COUNT"),
				createParams(
						"caseloadId", caseload,
						"username", username,
						"roleId", roleId),
				Long.class);

		return count != null && count > 0;
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
				createParams("caseloadId", caseload, "username", username, "startDate", DateTimeConverter.toDate(LocalDate.now())));
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
    @CacheEvict(value="findRolesByUsername", allEntries = true)
    public void addRole(String username, String caseload, Long roleId) {
		Validate.notBlank(caseload, "caseload is required.");
		Validate.notBlank(username, "username is required.");
		Validate.notNull(roleId, "roleId is required.");

        jdbcTemplate.update(
                getQuery("INSERT_USER_ROLE"),
                createParams("caseloadId", caseload, "username", username, "roleId", roleId));
	}

	@Override
    @CacheEvict(value="findRolesByUsername", allEntries = true)
	public void removeRole(String username, String caseload, Long roleId) {
		Validate.notBlank(caseload, "caseload is required.");
		Validate.notBlank(username, "username is required.");
		Validate.notNull(roleId, "roleId is required.");

        jdbcTemplate.update(
                getQuery("DELETE_USER_ROLE"),
                createParams("caseloadId", caseload, "username", username, "roleId", roleId));
	}

	@Override
	public List<UserDetail> findAllUsersWithCaseload(String caseloadId) {
		Validate.notBlank(caseloadId, "An caseload id is required.");

		String sql = getQuery("FIND_ACTIVE_STAFF_USERS_WITH_ACCESSIBLE_CASELOAD");

        return jdbcTemplate.query(
                sql,
                createParams("caseloadId", caseloadId),
                USER_DETAIL_ROW_MAPPER);
    }


    @Override
    public Page<UserDetail> findUsersByCaseload(String agencyId, String accessRole, String nameFilter, PageRequest pageRequest) {
        return getUsersByCaseload(agencyId, accessRole, nameFilter, pageRequest, "FIND_USERS_BY_CASELOAD");
    }

	@Override
	public Page<UserDetail> findLocalAdministratorUsersByCaseload(String agencyId, String accessRole, String nameFilter, PageRequest pageRequest) {
        return getUsersByCaseload(agencyId, accessRole, nameFilter, pageRequest, "FIND_LOCAL_ADMINISTRATOR_USERS_BY_CASELOAD");
    }

    private Page<UserDetail> getUsersByCaseload(String agencyId, String accessRole, String nameFilter, PageRequest pageRequest, String find_local_administrator_users_by_caseload) {
        Validate.notBlank(agencyId, "An agency id is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        String baseSql = applyAccessRoleQuery(applyNameFilterQuery(getQuery(find_local_administrator_users_by_caseload), nameFilter), accessRole);


        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(baseSql, USER_DETAIL_ROW_MAPPER.getFieldMap());
        String sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        PageAwareRowMapper<UserDetail> paRowMapper = new PageAwareRowMapper<>(USER_DETAIL_ROW_MAPPER);

        List<UserDetail> users = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "caseloadId", agencyId,
                        "nameFilter", nameFilter != null ? StringUtils.trimToEmpty(nameFilter.toUpperCase()) + "%" : null,
                        "apiCaseloadId", apiCaseloadId,
                        "applicationType", applicationType,
                        "roleCode", accessRole),
                paRowMapper);

        return new Page<>(users, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    private String applyNameFilterQuery(String baseSql, String nameFilter) {
        String nameFilterQuery = baseSql;

        if (StringUtils.isNotBlank(nameFilter)) {
            nameFilterQuery += NAME_FILTER_QUERY_TEMPLATE;
        }
        return nameFilterQuery;
    }

    private String applyAccessRoleQuery(String baseSql, String accessRole) {
        String resultSql = baseSql;

        if (StringUtils.isNotBlank(accessRole)) {

        	resultSql += APPLICATION_ROLE_CODE_FILTER_QUERY_TEMPLATE;
        }

        return resultSql;
    }
}
