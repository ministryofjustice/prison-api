package net.syscon.elite.aop;

import net.syscon.elite.persistence.impl.UserRepositoryImpl;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.util.SQLProvider;
import oracle.jdbc.driver.OracleConnection;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Properties;


@Aspect
public class OracleConnectionAspect {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private AspectJExpressionPointcutAdvisor closeConnectionAdvisor;
	private boolean enableProxy;
	private String rolePassword;
	
	@Inject
	public void setEnvironment(final Environment env) {
		enableProxy = env.getProperty("spring.datasource.hikari.oracle-proxy.enabled", Boolean.class);
		final String jdbcUrl = env.getProperty("spring.datasource.hikari.jdbc-url");
		final String username = env.getProperty("spring.datasource.hikari.username");
		final String password = env.getProperty("spring.datasource.hikari.password");
		final DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource(jdbcUrl, username, password);
		final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(driverManagerDataSource);
		try {
			final String filename = String.format("sqls/%s.sql", UserRepositoryImpl.class.getSimpleName());
			final SQLProvider sqlProvider = new SQLProvider();
			sqlProvider.loadFromClassLoader(filename);
			final String sql = sqlProvider.get("FIND_ROLE_PASSWORD");
			final MapSqlParameterSource params = new MapSqlParameterSource();
			
			final String encryptedPassword = jdbcTemplate.queryForObject(sql, params, String.class);
			params.addValue("password", encryptedPassword);
			rolePassword = jdbcTemplate.queryForObject("SELECT decryption('2DECRYPTPASSWRD', :password) FROM DUAL", params, String.class);
		} catch (final DataAccessException ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	@Pointcut("execution (* com.zaxxer.hikari.HikariDataSource.getConnection())")
	private void onNewConnectionPointcut() {
	}
	
	@PostConstruct
	public void postConstruct() {
		
		if (enableProxy) {
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
	}


	@Around ("onNewConnectionPointcut()")
	public Object connectionAround(final ProceedingJoinPoint joinPoint) throws Throwable {
	    if (log.isDebugEnabled()) {
	    	log.debug("Enter: {}.{}()", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
	    }
		try {
			final Connection conn = (Connection) joinPoint.proceed();
			final String username = UserSecurityUtils.getCurrentUsername();

			if (enableProxy && !StringUtils.equalsIgnoreCase(username, "ANON_USER")) {
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

		        if (log.isDebugEnabled()) {
		        	log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), conn);
		        }
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



