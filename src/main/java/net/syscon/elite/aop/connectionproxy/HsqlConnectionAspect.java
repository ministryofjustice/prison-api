package net.syscon.elite.aop.connectionproxy;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.security.AuthenticationFacade;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.SQLException;

@Aspect
@Slf4j
public class HsqlConnectionAspect extends AbstractConnectionAspect {
    private final AuthenticationFacade authenticationFacade;

    public HsqlConnectionAspect(final AuthenticationFacade authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    protected Connection openProxySessionIfIdentifiedAuthentication(final Connection pooledConnection) throws SQLException {
        if (authenticationFacade.isIdentifiedAuthentication()) {
            log.trace("Configuring Hsql Proxy Session.");
            return openAndConfigureProxySessionForConnection(pooledConnection);
        }
        return pooledConnection;
    }

    private Connection openAndConfigureProxySessionForConnection(final Connection pooledConnection) throws SQLException {

        openProxySessionForCurrentUsername(pooledConnection);
        return pooledConnection;
    }

    private void openProxySessionForCurrentUsername(final Connection pooledConnection) throws SQLException {

        // just check that the current user exists in the database
        try (final var statement = pooledConnection.prepareStatement("SELECT username FROM staff_user_accounts WHERE username = ?")) {
            final var currentUsername = authenticationFacade.getCurrentUsername();
            statement.setString(1, currentUsername);

            try {
                try (final var resultSet = statement.executeQuery()) {
                    Assert.isTrue(resultSet.next(), String.format("User %s not found", currentUsername));
                }

            } catch (final SQLException e) {
                log.error("User {} does not support Proxy Connection", currentUsername);
                pooledConnection.close();
                throw e;
            }
            log.debug("Proxy Connection for {} Successful", currentUsername);
        }
    }
}
