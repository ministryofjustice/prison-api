package uk.gov.justice.hmpps.prison.repository;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.model.UserDetailDto;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.Status;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.UserRepositorySql;
import uk.gov.justice.hmpps.prison.service.filters.NameFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

@Repository
public class UserRepository extends RepositoryBase {

    @Value("${application.caseload.id:NWEB}")
    private String apiCaseloadId;

    @Value("${application.type:APP}")
    private String applicationType;

    private final DataClassByColumnRowMapper<UserDetailDto> USER_DETAIL_ROW_MAPPER = new DataClassByColumnRowMapper<>(UserDetailDto.class);


    public Optional<UserDetail> findByUsername(final String username) {
        final var sql = UserRepositorySql.FIND_USER_BY_USERNAME.getSql();
        UserDetailDto userDetails;
        try {
            userDetails = jdbcTemplate.queryForObject(
                sql,
                createParams("username", username),
                USER_DETAIL_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            userDetails = null;
        }
        return Optional.ofNullable(userDetails).map(UserDetailDto::toUserDetail);
    }

    public void updateWorkingCaseLoad(final String username, final String caseLoadId) {
        final var sql = UserRepositorySql.UPDATE_STAFF_ACTIVE_CASE_LOAD.getSql();
        jdbcTemplate.update(sql, createParams("caseLoadId", caseLoadId, "username", username));
    }


    @Cacheable(value = "findByStaffIdAndStaffUserType", unless = "#result == null")
    public Optional<UserDetail> findByStaffIdAndStaffUserType(final Long staffId, final String staffUserType) {
        Validate.notNull(staffId, "Staff id is required.");
        Validate.notBlank(staffUserType, "Staff user type is required.");

        final var sql = UserRepositorySql.FIND_USER_BY_STAFF_ID_STAFF_USER_TYPE.getSql();

        UserDetailDto userDetail;

        try {
            userDetail = jdbcTemplate.queryForObject(
                sql,
                createParams("staffId", staffId, "staffUserType", staffUserType),
                USER_DETAIL_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            userDetail = null;
        }

        return Optional.ofNullable(userDetail).map(UserDetailDto::toUserDetail);
    }

    public List<UserDetail> findAllUsersWithCaseload(final String caseloadId, final String missingCaseloadId) {
        Validate.notBlank(caseloadId, "An caseload id is required.");

        final var sql = UserRepositorySql.FIND_ACTIVE_STAFF_USERS_WITH_ACCESSIBLE_CASELOAD.getSql();

        final var users = jdbcTemplate.query(
            sql,
            createParams("caseloadId", caseloadId, "missingCaseloadId", missingCaseloadId),
            USER_DETAIL_ROW_MAPPER);
        return users.stream().map(UserDetailDto::toUserDetail).collect(Collectors.toList());
    }


    public List<UserDetail> getUserListByUsernames(final List<String> usernames) {

        final var sql = UserRepositorySql.FIND_USERS_BY_USERNAMES.getSql();

        final var users = jdbcTemplate.query(
            sql,
            createParams("usernames", usernames),
            USER_DETAIL_ROW_MAPPER);
        return users.stream().map(UserDetailDto::toUserDetail).collect(Collectors.toList());
    }


    public Page<UserDetail> findUsersByCaseload(final String caseload, final List<String> accessRoles, final NameFilter nameFilter, final Status status, final String activeCaseload, final PageRequest pageRequest) {
        Validate.notBlank(caseload, "A caseload is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        return getUsersByCaseload(UserRepositorySql.FIND_USERS_BY_CASELOAD, nameFilter, accessRoles, status, pageRequest, caseload, activeCaseload, null);
    }

    private Page<UserDetail> getUsersByCaseload(final UserRepositorySql query, final NameFilter nameFilter, final List<String> accessRoles, final Status status, final PageRequest pageRequest, final String caseload, final String activeCaseload, final String laaUsername) {

        final var baseSql = new StringBuilder();
        final var paramRoleMap = new HashMap<String, String>();

        if (accessRoles == null || accessRoles.isEmpty()) {
            baseSql.append(applyNameFilterQuery(query.getSql(), nameFilter));
        } else {
            IntStream
                .range(0, accessRoles.size())
                .forEach(roleNumber -> {
                    baseSql.append(applyNameFilterQuery(query.getSql(), nameFilter));
                    baseSql.append(format(UserRepositorySql.APPLICATION_ROLE_CODE_FILTER_QUERY_TEMPLATE.getSql(), roleNumber));
                    if (roleNumber < accessRoles.size() - 1) {
                        baseSql.append(" INTERSECT ");
                    }
                    paramRoleMap.put(format("roleCode_%d", roleNumber), accessRoles.get(roleNumber));
                });
        }

        final var builder = queryBuilderFactory.getQueryBuilder(baseSql.toString(), USER_DETAIL_ROW_MAPPER.getFieldMap());
        final var sql = builder
            .addRowCount()
            .addOrderBy(pageRequest)
            .addPagination()
            .build();

        final var paRowMapper = new PageAwareRowMapper<>(USER_DETAIL_ROW_MAPPER);

        final var paramSource = createParamSource(pageRequest,
            "caseloadId", caseload,
            "activeCaseloadId", activeCaseload,
            "laaUsername", laaUsername,
            "activeFlag", "Y",
            "searchTerm", StringUtils.isNotBlank(nameFilter.getSearchTerm()) ? nameFilter.getSearchTerm() + "%" : null,
            "surname", StringUtils.isNotBlank(nameFilter.getSurname()) ? nameFilter.getSurname() + "%" : null,
            "firstName", StringUtils.isNotBlank(nameFilter.getFirstName()) ? nameFilter.getFirstName() + "%" : null,
            "apiCaseloadId", apiCaseloadId,
            "applicationType", applicationType,
            "status", status == null ? Status.ALL : status.getSqlName());

        paramSource.addValues(paramRoleMap);
        final var users = jdbcTemplate.query(sql, paramSource, paRowMapper)
            .stream().map(UserDetailDto::toUserDetail).collect(Collectors.toList());

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
}
