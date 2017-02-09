package net.syscon.elite.web.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.syscon.elite.persistence.repository.AgencyRepository;
import net.syscon.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = AgencyRepository.class)
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
			throw new PersistenceException(ex.getMessage(), ex);
		}
	}

	@Bean
	public NamedParameterJdbcOperations namedJdbcTemplate(final DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	@Bean
	public EntityManager entityManager(final EntityManagerFactory entityManagerFactory) {
		final EntityManager entityManager =  entityManagerFactory.createEntityManager();
		return entityManager;
	}

	@Bean
	public EntityManagerFactory entityManagerFactory(final ConfigurableEnvironment env, final DataSource dataSource) {
		try {
			final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
			entityManagerFactoryBean.setDataSource(dataSource);
			entityManagerFactoryBean.setPersistenceUnitName("elite2-pu");
			entityManagerFactoryBean.setPackagesToScan("net.syscon.elite.persistence.domain");

			final Map<String, Object> configsMap = new HashMap<>();
			final String prefix = "spring.jpa.properties.";
			PropertiesUtil.toProperties(env, prefix).entrySet().forEach(entry -> {
				final String key = StringUtils.replace(entry.getKey().toString(), prefix, "");
				configsMap.put(key, entry.getValue());
			});
			entityManagerFactoryBean.setJpaPropertyMap(configsMap);
			final HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
			entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);
			entityManagerFactoryBean.afterPropertiesSet();
			entityManagerFactoryBean.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());

			return entityManagerFactoryBean.getObject();

		} catch (final Throwable ex) {
			LOG.error(ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}


	@Bean
	public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory);
		return transactionManager;
	}


}
