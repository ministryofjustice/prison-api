package uk.gov.justice.hmpps.prison.repository;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.AccessRole;
import uk.gov.justice.hmpps.prison.api.model.StaffUserRole;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.model.UserRole;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.UserRepositorySql;
import uk.gov.justice.hmpps.prison.service.filters.NameFilter;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends RepositoryBase {

    @Value("${application.caseload.id:NWEB}")
    private String apiCaseloadId;

    @Value("${application.type:APP}")
    private String applicationType;

    private static final String ADMIN_ROLE_FUNCTION = "ADMIN";

    private final StandardBeanPropertyRowMapper<UserRole> USER_ROLE_MAPPER =
            new StandardBeanPropertyRowMapper<>(UserRole.class);

    private final StandardBeanPropertyRowMapper<AccessRole> ACCESS_ROLE_MAPPER =
            new StandardBeanPropertyRowMapper<>(AccessRole.class);

    private final StandardBeanPropertyRowMapper<StaffUserRole> STAFF_USER_ROLE_MAPPER =
            new StandardBeanPropertyRowMapper<>(StaffUserRole.class);

    private final StandardBeanPropertyRowMapper<UserDetail> USER_DETAIL_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(UserDetail.class);


    public Optional<UserDetail> findByUsername(final String username) {
        final var sql = UserRepositorySql.FIND_USER_BY_USERNAME.getSql();
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


    @Cacheable("findRolesByUsername")
    public List<UserRole> findRolesByUsername(final String username, final String query) {
        var builder = queryBuilderFactory.getQueryBuilder(UserRepositorySql.FIND_ROLES_BY_USERNAME.getSql(), USER_ROLE_MAPPER);

        if (StringUtils.isNotBlank(query)) {
            builder = builder.addQuery(query);
        }
        final var sql = builder
                .addOrderBy(Order.ASC, "roleCode")
                .build();

        return jdbcTemplate.query(sql, createParams("username", username), USER_ROLE_MAPPER);
    }


    public List<AccessRole> findAccessRolesByUsernameAndCaseload(final String username, final String caseload, final boolean includeAdmin) {
        var query = UserRepositorySql.FIND_ACCESS_ROLES_BY_USERNAME_AND_CASELOAD.getSql();

        if (!includeAdmin) query += UserRepositorySql.EXCLUDE_BY_ROLE_FUNCTION_CLAUSE.getSql();

        final var builder = queryBuilderFactory.getQueryBuilder(query, ACCESS_ROLE_MAPPER);

        final var sql = builder
                .addOrderBy(Order.ASC, "roleName")
                .build();

        return jdbcTemplate.query(sql, createParams("username", username, "caseloadId", caseload, "roleFunction", ADMIN_ROLE_FUNCTION), ACCESS_ROLE_MAPPER);
    }


    public void updateWorkingCaseLoad(final String username, final String caseLoadId) {
        final var sql = UserRepositorySql.UPDATE_STAFF_ACTIVE_CASE_LOAD.getSql();
        jdbcTemplate.update(sql, createParams("caseLoadId", caseLoadId, "username", username));
    }


    @Cacheable("findByStaffIdAndStaffUserType")
    public Optional<UserDetail> findByStaffIdAndStaffUserType(final Long staffId, final String staffUserType) {
        Validate.notNull(staffId, "Staff id is required.");
        Validate.notBlank(staffUserType, "Staff user type is required.");

        final var sql = UserRepositorySql.FIND_USER_BY_STAFF_ID_STAFF_USER_TYPE.getSql();

        UserDetail userDetail;

        try {
            userDetail = jdbcTemplate.queryForObject(
                    sql,
                    createParams("staffId", staffId, "staffUserType", staffUserType),
                    USER_DETAIL_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            userDetail = null;
        }

        return Optional.ofNullable(userDetail);
    }


    public boolean isRoleAssigned(final String username, final String caseload, final long roleId) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(username, "username is required.");

        final var count = jdbcTemplate.queryForObject(
                UserRepositorySql.ROLE_ASSIGNED_COUNT.getSql(),
                createParams(
                        "caseloadId", caseload,
                        "username", username,
                        "roleId", roleId),
                Long.class);

        return count != null && count > 0;
    }


    public boolean isUserAssessibleCaseloadAvailable(final String caseload, final String username) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(username, "username is required.");

        final var count = jdbcTemplate.queryForObject(
                UserRepositorySql.USER_ACCESSIBLE_CASELOAD_COUNT.getSql(),
                createParams("caseloadId", caseload, "username", username),
                Long.class);

        return count != null && count > 0;
    }


    public Optional<Long> getRoleIdForCode(final String roleCode) {
        Validate.notBlank(roleCode, "roleCode is required.");

        Long roleId;
        try {
            roleId = jdbcTemplate.queryForObject(
                    UserRepositorySql.GET_ROLE_ID_FOR_ROLE_CODE.getSql(),
                    createParams("roleCode", roleCode),
                    Long.class);

        } catch (final EmptyResultDataAccessException ex) {
            roleId = null;
        }
        return Optional.ofNullable(roleId);
    }


    public Optional<AccessRole> getRoleByCode(final String roleCode) {
        Validate.notBlank(roleCode, "roleCode is required.");

        AccessRole role;
        try {
            role = jdbcTemplate.queryForObject(
                    UserRepositorySql.GET_ROLE_BY_ROLE_CODE.getSql(),
                    createParams("roleCode", roleCode),
                    ACCESS_ROLE_MAPPER);

        } catch (final EmptyResultDataAccessException ex) {
            role = null;
        }
        return Optional.ofNullable(role);
    }



    public void addUserAssessibleCaseload(final String caseload, final String username) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(username, "username is required.");

        jdbcTemplate.update(
                UserRepositorySql.USER_ACCESSIBLE_CASELOAD_INSERT.getSql(),
                createParams("caseloadId", caseload, "username", username, "startDate", DateTimeConverter.toDate(LocalDate.now())));
    }


    public List<StaffUserRole> getAllStaffRolesForCaseload(final String caseload, final String roleCode) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(roleCode, "roleCode is required.");

        return jdbcTemplate.query(UserRepositorySql.FIND_ROLES_BY_CASELOAD_AND_ROLE.getSql(),
                createParams("caseloadId", caseload, "roleCode", roleCode),
                STAFF_USER_ROLE_MAPPER);

    }


    @CacheEvict(value = "findRolesByUsername", allEntries = true)
    public void addRole(final String username, final String caseload, final Long roleId) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(username, "username is required.");
        Validate.notNull(roleId, "roleId is required.");

        jdbcTemplate.update(
                UserRepositorySql.INSERT_USER_ROLE.getSql(),
                createParams("caseloadId", caseload, "username", username, "roleId", roleId));
    }


    @CacheEvict(value = "findRolesByUsername", allEntries = true)
    public int removeRole(final String username, final String caseload, final Long roleId) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(username, "username is required.");
        Validate.notNull(roleId, "roleId is required.");

        return jdbcTemplate.update(
                UserRepositorySql.DELETE_USER_ROLE.getSql(),
                createParams("caseloadId", caseload, "username", username, "roleId", roleId));
    }


    public List<UserDetail> findAllUsersWithCaseload(final String caseloadId, final String missingCaseloadId) {
        Validate.notBlank(caseloadId, "An caseload id is required.");

        final var sql = UserRepositorySql.FIND_ACTIVE_STAFF_USERS_WITH_ACCESSIBLE_CASELOAD.getSql();

        return jdbcTemplate.query(
                sql,
                createParams("caseloadId", caseloadId, "missingCaseloadId", missingCaseloadId),
                USER_DETAIL_ROW_MAPPER);
    }


    public List<UserDetail> getUserListByUsernames(final List<String> usernames) {

        final var sql = UserRepositorySql.FIND_USERS_BY_USERNAMES.getSql();

        return jdbcTemplate.query(
                sql,
                createParams("usernames", usernames),
                USER_DETAIL_ROW_MAPPER);
    }



    public Page<UserDetail> findUsersByCaseload(final String caseload, final String accessRole, final NameFilter nameFilter, final PageRequest pageRequest) {
        Validate.notBlank(caseload, "An caseload id is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        return getUsersByCaseload(UserRepositorySql.FIND_USERS_BY_CASELOAD, nameFilter, accessRole, pageRequest, caseload, null);
    }


    public Page<UserDetail> findUsers(final String accessRole, final NameFilter nameFilter, final PageRequest pageRequest) {
        Validate.notNull(pageRequest, "Page request details are required.");

        return getUsersByCaseload(UserRepositorySql.FIND_USERS, nameFilter, accessRole, pageRequest, null, null);
    }


    public Page<UserDetail> getUsersAsLocalAdministrator(final String laaUsername, final String accessRole, final NameFilter nameFilter, final PageRequest pageRequest) {
        Validate.notBlank(laaUsername, "A username is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        return getUsersByCaseload(UserRepositorySql.FIND_USERS_AVAILABLE_TO_LAA_USER, nameFilter, accessRole, pageRequest, null, laaUsername);
    }

    private Page<UserDetail> getUsersByCaseload(final UserRepositorySql query, final NameFilter nameFilter, final String accessRole, final PageRequest pageRequest, final String caseload, final String laaUsername) {
        final var baseSql = applyAccessRoleQuery(applyNameFilterQuery(query.getSql(), nameFilter), accessRole);


        final var builder = queryBuilderFactory.getQueryBuilder(baseSql, USER_DETAIL_ROW_MAPPER.getFieldMap());
        final var sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(USER_DETAIL_ROW_MAPPER);

        final var users = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest,
                    "caseloadId", caseload,
                    "laaUsername", laaUsername,
                    "activeFlag", "Y",
                    "searchTerm", StringUtils.isNotBlank(nameFilter.getSearchTerm()) ? nameFilter.getSearchTerm() + "%" : null,
                    "surname", StringUtils.isNotBlank(nameFilter.getSurname()) ? nameFilter.getSurname() + "%" : null,
                    "firstName", StringUtils.isNotBlank(nameFilter.getFirstName()) ? nameFilter.getFirstName() + "%" : null,
                    "apiCaseloadId", apiCaseloadId,
                    "applicationType", applicationType,
                    "roleCode", accessRole),
                paRowMapper);

        return new Page<>(users, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    private String applyNameFilterQuery(final String baseSql, final NameFilter nameFilter) {
        var nameFilterQuery = baseSql;

        if (nameFilter.isProvided()) {
            if (nameFilter.isFullNameSearch()) {
                nameFilterQuery += UserRepositorySql.FULL_NAME_FILTER_QUERY_TEMPLATE.getSql();
            } else {
                nameFilterQuery += UserRepositorySql.NAME_FILTER_QUERY_TEMPLATE.getSql();
            }
        }
        return nameFilterQuery;
    }

    private String applyAccessRoleQuery(final String baseSql, final String accessRole) {
        var resultSql = baseSql;

        if (StringUtils.isNotBlank(accessRole)) {

            resultSql += UserRepositorySql.APPLICATION_ROLE_CODE_FILTER_QUERY_TEMPLATE.getSql();
        }

        return resultSql;
    }
}
