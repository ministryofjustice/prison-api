package net.syscon.prison.aop.connectionproxy;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

import static java.lang.String.format;

@Slf4j
public class RoleConfigurer {

    private final RolePasswordSupplier rolePasswordSupplier;

    private final String tagUser;

    public RoleConfigurer(
            final String tagUser,
            final RolePasswordSupplier rolePasswordSupplier
    ) {
        this.tagUser = tagUser;
        this.rolePasswordSupplier = rolePasswordSupplier;
    }

    protected void setRoleForConnection(final Connection conn) throws SQLException {

        final var startSessionSQL = format(
                "SET ROLE %s IDENTIFIED BY %s",
                tagUser,
                rolePasswordSupplier.getRolePassword());

        try (final var stmt = conn.prepareStatement(startSessionSQL)) {
            stmt.execute();
        }
    }

}
