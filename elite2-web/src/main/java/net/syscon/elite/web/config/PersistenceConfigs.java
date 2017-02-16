package net.syscon.elite.web.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.syscon.elite.persistence.repository.AgencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import javax.sql.DataSource;

@Configuration
@EnableCaching
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {AgencyRepository.class })
public class PersistenceConfigs {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Bean
	public DataSource dataSource(final ConfigurableEnvironment env) {
		try {
			final HikariConfig config = new HikariConfig();
			config.setPoolName(env.getProperty("spring.datasource.hikari.pool-name"));
			config.setDriverClassName(env.getProperty("spring.datasource.hikari.driver-class-name"));
			config.setIdleTimeout(env.getProperty("spring.datasource.hikari.idle-timeout", Long.class));
			config.setMaximumPoolSize(env.getProperty("spring.datasource.hikari.maximum-pool-size", Integer.class));
			config.setUsername(env.getProperty("spring.datasource.hikari.username"));
			config.setPassword(env.getProperty("spring.datasource.hikari.password"));
			config.setJdbcUrl(env.getProperty("spring.datasource.hikari.jdbc-url"));
			config.setConnectionInitSql(env.getProperty("spring.datasource.hikari.connection-init-sql"));
			config.setConnectionTestQuery(env.getProperty("spring.datasource.hikari.connection-test-query"));
			final DataSource dataSource = new HikariDataSource(config);
			return dataSource;
		} catch (final Exception ex) {
			LOG.error(ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	@Bean
	public NamedParameterJdbcOperations namedJdbcTemplate(final DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	@Bean
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		return transactionManager;
	}


}
