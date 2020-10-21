package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.AccessRole;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class AccessRoleRepository extends RepositoryBase {

    private static final String EXCLUDE_ADMIN_ROLES_QUERY_TEMPLATE = " AND OMS_ROLES.ROLE_FUNCTION != 'ADMIN'";

    private static final StandardBeanPropertyRowMapper<AccessRole> ACCESS_ROLE_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(AccessRole.class);

    public void createAccessRole(final AccessRole accessRole) {
        Objects.requireNonNull(accessRole.getRoleName(), "Access role name is a required parameter");
        Objects.requireNonNull(accessRole.getRoleCode(), "Access role code is a required parameter");
        Objects.requireNonNull(accessRole.getRoleFunction(), "Access role function is a required parameter");

        jdbcTemplate.update(
                getQuery("INSERT_ACCESS_ROLE"),
                createParams("roleCode", accessRole.getRoleCode(), "roleName", accessRole.getRoleName(), "parentRoleCode", accessRole.getParentRoleCode(), "roleFunction", accessRole.getRoleFunction()));
    }

    public void updateAccessRole(final AccessRole accessRole) {

        final var query = "UPDATE_ACCESS_ROLE";

        jdbcTemplate.update(
                getQuery(query),
                createParams("roleCode", accessRole.getRoleCode(), "roleName", accessRole.getRoleName(), "roleFunction", accessRole.getRoleFunction()));
    }

    public Optional<AccessRole> getAccessRole(final String accessRoleCode) {
        Objects.requireNonNull(accessRoleCode, "Access role code is a required parameter");
        AccessRole accessRole;
        try {
            accessRole = jdbcTemplate.queryForObject(
                    getQuery("GET_ACCESS_ROLE"),
                    createParams("roleCode", accessRoleCode),
                    ACCESS_ROLE_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            accessRole = null;
        }
        return Optional.ofNullable(accessRole);
    }

    public List<AccessRole> getAccessRoles(final boolean includeAdmin) {

        var query = getQuery("GET_ACCESS_ROLES");
        if (!includeAdmin) {
            query += EXCLUDE_ADMIN_ROLES_QUERY_TEMPLATE;
        }
        final var builder = queryBuilderFactory.getQueryBuilder(query, ACCESS_ROLE_ROW_MAPPER);
        final var sql = builder.addOrderBy(Order.ASC, "roleName").build();

        return jdbcTemplate.query(sql, ACCESS_ROLE_ROW_MAPPER);
    }
}
