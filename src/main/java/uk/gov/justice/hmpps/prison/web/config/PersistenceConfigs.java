package uk.gov.justice.hmpps.prison.web.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.Map;

@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = {"uk.gov.justice.hmpps.prison.repository", "uk.gov.justice.hmpps.prison.util"})
@Log4j2
public class PersistenceConfigs {

    private static final String PRIMARY_DATASOURCE_PREFIX = "spring.datasource";
    private static final String REPLICA_DATASOURCE_PREFIX = "spring.replica.datasource";

    private final Environment environment;

    public PersistenceConfigs(final Environment environment) {
        this.environment = environment;
    }

    /**
     * Provide a Clock instance. This is an external source of time, so effectively a read-only repository.
     *
     * @return clock
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public DataSource readWriteDataSource() {
        return buildDataSource("PrimaryHikariPool", PRIMARY_DATASOURCE_PREFIX, false);
    }

    @Bean
    @ConditionalOnProperty("spring.replica.datasource.url")
    public DataSource replicaDataSource() {
        return buildDataSource("ReplicaHikariPool", REPLICA_DATASOURCE_PREFIX, true);
    }

    @Bean
    @ConditionalOnProperty("spring.replica.datasource.url")
    @Primary
    public DataSource dataSource(@Qualifier("readWriteDataSource") final DataSource readWriteDataSource,
                                 @Qualifier("replicaDataSource") final DataSource replicaDataSource) {
        final var routingDataSource = new RoutingDataSource();
        final var targetDataSources = Map.<Object, Object>of(RoutingDataSource.Route.PRIMARY, readWriteDataSource, RoutingDataSource.Route.REPLICA, replicaDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(readWriteDataSource);

        return routingDataSource;
    }

    private DataSource buildDataSource(final String poolName, final String dataSourcePrefix, final boolean readonly) {

        final var url = environment.getProperty(String.format("%s.url", dataSourcePrefix));
        final var hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(poolName);

        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(environment.getProperty(String.format("%s.username", dataSourcePrefix)));
        hikariConfig.setPassword(environment.getProperty(String.format("%s.password", dataSourcePrefix)));

        final var maxPoolSize = environment.getProperty(String.format("%s.hikari.maximum-pool-size", dataSourcePrefix));
        if (StringUtils.isNotBlank(maxPoolSize)) {
            log.info("Setting maximum pool size to be {}", maxPoolSize);
            hikariConfig.setMaximumPoolSize(Integer.parseInt(maxPoolSize));
        }
        final var maxLifetime = environment.getProperty(String.format("%s.hikari.max-lifetime", dataSourcePrefix));
        if (StringUtils.isNotBlank(maxLifetime)) {
            log.info("Setting max lifetime to be {}", maxLifetime);
            hikariConfig.setMaxLifetime(Integer.parseInt(maxLifetime));
        }
        final var connectionTimeout = environment.getProperty(String.format("%s.hikari.connectionTimeout", dataSourcePrefix));
        if (StringUtils.isNotBlank(connectionTimeout)) {
            hikariConfig.setConnectionTimeout(Integer.parseInt(connectionTimeout));
        }
        final var validationTimeout = environment.getProperty(String.format("%s.hikari.validationTimeout", dataSourcePrefix));
        if (StringUtils.isNotBlank(validationTimeout)) {
            hikariConfig.setValidationTimeout(Integer.parseInt(validationTimeout));
        }

        hikariConfig.setReadOnly(readonly);
        return new HikariDataSource(hikariConfig);
    }
}
