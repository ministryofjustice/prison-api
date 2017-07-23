package net.syscon.elite.aop;

import net.syscon.elite.security.UserSecurityUtils;
import oracle.jdbc.driver.OracleConnection;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.aop.framework.ProxyFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Properties;


@Aspect
public class OracleConnectionAspect {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final AspectJExpressionPointcutAdvisor closeConnectionAdvisor;
	private final String rolePassword;

	public OracleConnectionAspect(String rolePassword) {
	    this.rolePassword = rolePassword;
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
    private void onNewConnectionPointcut() {
    }

    @Around ("onNewConnectionPointcut()")
	public Object connectionAround(final ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Enter: {}.{}()", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
		try {
			final Connection conn = (Connection) joinPoint.proceed();
            final String username = UserSecurityUtils.getCurrentUsername();
			if (!UserSecurityUtils.isAnonymousAuthentication() && username != null && rolePassword != null) {
				final OracleConnection oracleConn = (OracleConnection) conn.unwrap(Connection.class);

				final Properties info = new Properties();

			    info.put(OracleConnection.PROXY_USER_NAME, username);
		        oracleConn.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, info);
		        
		        final ProxyFactory proxyFactory = new ProxyFactory(conn);
		        proxyFactory.addAdvisor(closeConnectionAdvisor);
		        final Connection proxyConn = (Connection) proxyFactory.getProxy();

                final String startSessionSQL = "SET ROLE TAG_USER IDENTIFIED BY " + rolePassword;
                final PreparedStatement stmt = oracleConn.prepareStatement(startSessionSQL);
                stmt.execute();
                stmt.close();

                log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), conn);
		        return proxyConn;
			} else {
				return conn;
			}
		} catch (final Throwable e) {
			log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()), joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), e);
			throw e;
		}
	}
}



