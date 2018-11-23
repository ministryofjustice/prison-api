package net.syscon.elite.aop.connectionproxy;

import lombok.extern.slf4j.Slf4j;
import net.syscon.util.SQLProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;

import static java.lang.String.format;

@Slf4j
public class RolePasswordSupplier {
    private final SQLProvider sqlProvider;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final String defaultSchema;

    /**
     * The role password.
     *
     * This is retrieved lazily from the database - providing resilience if the first
     * attempt fails. The reference is never updated once it is non-null.
     * Because RolePasswordSupplier used as a singleton the getRolePassword method below must be thread-safe.
     * Making the rolePassword field volatile ensures that writes to this reference 'happen before'
     * subsequent reads. This should be sufficient.
     */
    private volatile String rolePassword;

    public RolePasswordSupplier(
            SQLProvider sqlProvider,
            NamedParameterJdbcTemplate jdbcTemplate,
            String defaultSchema
    ) {
        this.sqlProvider = sqlProvider;
        this.jdbcTemplate = jdbcTemplate;
        this.defaultSchema = defaultSchema;
    }

    protected String getRolePassword() {
        if (rolePassword == null) {
            retrieveRolePassword();
        }
        return rolePassword;
    }

    private void retrieveRolePassword() {
        log.debug("Retrieving role password");

        final String encryptedPassword = getEncryptedPassword();
        rolePassword = decryptPassword(encryptedPassword);
    }

    private String getEncryptedPassword() {
        final String sql = format(sqlProvider.get("FIND_ROLE_PASSWORD"), replaceSchema());
        return jdbcTemplate.queryForObject(sql, Collections.emptyMap(), String.class);
    }

    private String decryptPassword(String encryptedPassword) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("password", encryptedPassword);
        return jdbcTemplate.queryForObject(format("SELECT %sdecryption('2DECRYPTPASSWRD', :password) FROM DUAL", replaceSchema()), params, String.class);
    }

    private String replaceSchema() {
        return StringUtils.isNotBlank(defaultSchema) ? defaultSchema + "." : "";
    }
}
