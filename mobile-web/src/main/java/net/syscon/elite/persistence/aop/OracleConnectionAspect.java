package net.syscon.elite.persistence.aop;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Properties;

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

import net.syscon.elite.persistence.security.UserInfoProvider;
import oracle.jdbc.driver.OracleConnection;


@Aspect
public class OracleConnectionAspect {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final AspectJExpressionPointcutAdvisor closeConnectionAdvisor;
	
	
	private UserInfoProvider userInfoProvider;
	
	@Inject
	public void setUserInfoProvider(final UserInfoProvider userInfoProvider) {
		this.userInfoProvider = userInfoProvider;
	}
	
	
	public static class CloseConnectionAdvice implements MethodBeforeAdvice {
		@Override
		public void before(final Method method, final Object[] args, final Object target) throws Throwable {
			if (method.getName().equals("close")) {
				final Connection conn = (Connection) target;
				final OracleConnection oracleConn = (OracleConnection) conn.unwrap(Connection.class);
				oracleConn.close(OracleConnection.PROXY_SESSION);
			}
		}
	}
	
	public OracleConnectionAspect() {
		closeConnectionAdvisor = new AspectJExpressionPointcutAdvisor();
		closeConnectionAdvisor.setExpression("execution (* java.sql.Connection.close(..))");
		closeConnectionAdvisor.setAdvice(new CloseConnectionAdvice());
		closeConnectionAdvisor.setOrder(10);
	}
		

	@Pointcut("execution (* com.zaxxer.hikari.HikariDataSource.getConnection())")
	private void onNewConnectionPointcut() {
	}
	

	@Around ("onNewConnectionPointcut()")
	public Object logAround(final ProceedingJoinPoint joinPoint) throws Throwable {
	    if (log.isDebugEnabled()) {
	    	log.debug("Enter: {}.{}()", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
	    }
		try {
			final Connection result = (Connection) joinPoint.proceed();
			final OracleConnection oracleConn = (OracleConnection) result.unwrap(Connection.class);
			final Properties properties = userInfoProvider.getUserInfo();
	        oracleConn.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, properties);
	        
	        final ProxyFactory proxyFactory = new ProxyFactory(result);
	        proxyFactory.addAdvisor(closeConnectionAdvisor);
	        final Connection proxiedConnection = (Connection) proxyFactory.getProxy();
	        if (log.isDebugEnabled()) {
	        	log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), result);
	        }
	        return proxiedConnection;

		} catch (final IllegalArgumentException e) {
			log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()), joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
			throw e;
		}
	}
	
}



