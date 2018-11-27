package net.syscon.elite.aop.connectionproxy;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.lang.String.format;

@Slf4j
public class RoleConfigurer {

    private final RolePasswordSupplier rolePasswordSupplier;

    private final String tagUser;

    public RoleConfigurer(
            String tagUser,
            RolePasswordSupplier rolePasswordSupplier
    ) {
        this.tagUser = tagUser;
        this.rolePasswordSupplier = rolePasswordSupplier;
    }

    protected void setRoleForConnection(Connection conn) throws SQLException {

        final String startSessionSQL = format(
                "SET ROLE %s IDENTIFIED BY %s",
                tagUser,
                rolePasswordSupplier.getRolePassword());

        try (PreparedStatement stmt = conn.prepareStatement(startSessionSQL)) {
            stmt.execute();
        }
    }

}
