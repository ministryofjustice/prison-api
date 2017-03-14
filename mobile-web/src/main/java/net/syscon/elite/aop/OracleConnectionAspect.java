package net.syscon.elite.aop;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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

import net.syscon.elite.security.UserInfoProvider;
import oracle.jdbc.driver.OracleConnection;


@Aspect
public class OracleConnectionAspect {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private AspectJExpressionPointcutAdvisor closeConnectionAdvisor;
	private UserInfoProvider userInfoProvider;
	private Environment env;
	
	@Inject
	public void setUserInfoProvider(final UserInfoProvider userInfoProvider) {
		this.userInfoProvider = userInfoProvider;
	}
	
	@Inject
	public void setEnvironment(final Environment env) {
		this.env = env;
	}
	
	@Pointcut("execution (* com.zaxxer.hikari.HikariDataSource.getConnection())")
	private void onNewConnectionPointcut() {
	}
	
	@PostConstruct
	public void postConstruct() {
		closeConnectionAdvisor = new AspectJExpressionPointcutAdvisor();
		closeConnectionAdvisor.setExpression("execution (* java.sql.Connection.close(..))");
		closeConnectionAdvisor.setOrder(10);
		closeConnectionAdvisor.setAdvice((MethodBeforeAdvice) (method, args, target) -> {
			if (env.getProperty("spring.datasource.hikari.oracle-proxy", Boolean.class)) {
				if (method.getName().equals("close")) {
					final Connection conn = (Connection) target;
					final OracleConnection oracleConn = (OracleConnection) conn.unwrap(Connection.class);
					oracleConn.close(OracleConnection.PROXY_SESSION);
				}
			}
		});
	}


	@Around ("onNewConnectionPointcut()")
	public Object logAround(final ProceedingJoinPoint joinPoint) throws Throwable {
	    if (log.isDebugEnabled()) {
	    	log.debug("Enter: {}.{}()", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
	    }
		try {
			final Connection conn = (Connection) joinPoint.proceed();
			if (env.getProperty("spring.datasource.hikari.oracle-proxy", Boolean.class)) {
				final OracleConnection oracleConn = (OracleConnection) conn.unwrap(Connection.class);
				final Properties properties = userInfoProvider.getUserInfo();
		        oracleConn.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, properties);
		        oracleConn.setSchema(env.getProperty("spring.datasource.hikari.username", String.class));
		        final ProxyFactory proxyFactory = new ProxyFactory(conn);
		        proxyFactory.addAdvisor(closeConnectionAdvisor);
		        final Connection proxyConn = (Connection) proxyFactory.getProxy();
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



