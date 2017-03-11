package net.syscon.elite.persistence.proxy;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import oracle.jdbc.driver.OracleConnection;

public class ProxyDataSource implements DataSource {

	private final HikariDataSource hikariDataSource;
	private final UserInfoProvider userInfoProvider;
	private final Pointcut pointCut = new CloseConnectionPointCut();
    private final Advice advice = new CloseConnectionAdvice();
    private final Advisor advisor = new DefaultPointcutAdvisor(pointCut, advice);
	

	public ProxyDataSource(final HikariConfig configuration, final UserInfoProvider userInfoProvider) {
		this.userInfoProvider = userInfoProvider;
		this.hikariDataSource = new HikariDataSource(configuration);

	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return hikariDataSource.getLogWriter();
	}

	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		return hikariDataSource.unwrap(iface);
	}

	@Override
	public void setLogWriter(final PrintWriter out) throws SQLException {
		hikariDataSource.setLogWriter(out);
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		return hikariDataSource.isWrapperFor(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {
		final Connection conn = hikariDataSource.getConnection();
		final OracleConnection oracleConn = (OracleConnection) conn.unwrap(Connection.class);


		final Properties properties = new Properties();
        properties.put("PROXY_USER_NAME", "wellington");

        oracleConn.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, properties);
        

		final ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.addAdvisor(advisor);
		proxyFactory.setTarget(conn);

		final Connection proxyConn = (Connection) proxyFactory.getProxy();
		return proxyConn;

	}

	@Override
	public void setLoginTimeout(final int seconds) throws SQLException {
		hikariDataSource.setLoginTimeout(seconds);
	}

	@Override
	public Connection getConnection(final String username, final String password) throws SQLException {
		return hikariDataSource.getConnection(username, password);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return hikariDataSource.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return hikariDataSource.getParentLogger();
	}

}
