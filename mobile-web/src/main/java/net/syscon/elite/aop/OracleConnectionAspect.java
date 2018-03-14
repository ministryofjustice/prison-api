package net.syscon.elite.aop;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.util.SQLProvider;
import oracle.jdbc.driver.OracleConnection;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static java.lang.String.format;

@Aspect
@Slf4j
public class OracleConnectionAspect {
    private final AuthenticationFacade authenticationFacade;
    private final AspectJExpressionPointcutAdvisor closeConnectionAdvisor;
    private String rolePassword;
    private final SQLProvider sqlProvider;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String tagUser;
    private final String defaultSchema;

    public OracleConnectionAspect(AuthenticationFacade authenticationFacade, SQLProvider sqlProvider,
                                  String jdbcUrl, String username, String password, String tagUser,
                                  String defaultSchema) {
        this.authenticationFacade = authenticationFacade;
        this.sqlProvider = sqlProvider;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.tagUser = tagUser;
        this.defaultSchema = defaultSchema;

        closeConnectionAdvisor = new AspectJExpressionPointcutAdvisor();
        closeConnectionAdvisor.setExpression("execution (* java.sql.Connection.close(..))");
        closeConnectionAdvisor.setOrder(10);

        closeConnectionAdvisor.setAdvice((MethodBeforeAdvice) (method, args, target) -> {
            if (method.getName().equals("close")) {
                final Connection conn = (Connection) target;
                final OracleConnection oracleConn = (OracleConnection) conn.unwrap(Connection.class);

                oracleConn.close(OracleConnection.PROXY_SESSION);
            }
        });
    }

    @Pointcut("execution (* com.zaxxer.hikari.HikariDataSource.getConnection())")
    protected void onNewConnectionPointcut() {
        // No code needed
    }

    @Around("onNewConnectionPointcut()")
    public Object connectionAround(final ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Enter: {}.{}()", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        try {
            final Connection conn = (Connection) joinPoint.proceed();
            if (authenticationFacade.isIdentifiedAuthentication()) {

                final OracleConnection oracleConn = (OracleConnection) conn.unwrap(Connection.class);
                final Properties info = new Properties();

                info.put(OracleConnection.PROXY_USER_NAME, authenticationFacade.getCurrentUsername());
                oracleConn.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, info);

                final ProxyFactory proxyFactory = new ProxyFactory(conn);

                proxyFactory.addAdvisor(closeConnectionAdvisor);

                final Connection proxyConn = (Connection) proxyFactory.getProxy();

                setDefaultSchema(proxyConn);
                setRolePrivs(oracleConn);

                log.debug(
                        "Exit: {}.{}()",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName());
                return proxyConn;
            } else {
                return conn;
            }
        } catch (final Throwable e) {
            log.error(
                    "Exception thrown in OracleConnectionAspect.connectionAround(), join point {}.{}(): {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage());

            throw e;
        }
    }

    private void setRolePrivs(Connection conn) throws SQLException {
        assignRolePassword();

        final String startSessionSQL = format("SET ROLE %s IDENTIFIED BY %s", tagUser, rolePassword);
        final PreparedStatement stmt = conn.prepareStatement(startSessionSQL);

        stmt.execute();
        stmt.close();
    }

    private void setDefaultSchema(final Connection conn) throws SQLException {
        if (StringUtils.isNotBlank(defaultSchema)) {
            try (PreparedStatement ps = conn.prepareStatement(format("ALTER SESSION SET CURRENT_SCHEMA=%s", defaultSchema))) {
                ps.execute();
            }
        }
    }

    private void assignRolePassword() {
        if (rolePassword == null) {
            final DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource(jdbcUrl, username, password);
            final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(driverManagerDataSource);
            final String sql = format(sqlProvider.get("FIND_ROLE_PASSWORD"), replaceSchema());
            final MapSqlParameterSource params = new MapSqlParameterSource();
            final String encryptedPassword = jdbcTemplate.queryForObject(sql, params, String.class);

            params.addValue("password", encryptedPassword);

            rolePassword = jdbcTemplate.queryForObject(format("SELECT %sdecryption('2DECRYPTPASSWRD', :password) FROM DUAL", replaceSchema()), params, String.class);
        }
    }

    private String replaceSchema() {
        return StringUtils.isNotBlank(defaultSchema) ? defaultSchema + "." : "";
    }
}
