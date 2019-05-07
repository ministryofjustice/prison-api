package net.syscon.elite.web.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.HashMap;

@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScan(basePackages = { "net.syscon.elite.repository", "net.syscon.util" })
public class PersistenceConfigs {

    private static final String PRIMARY_DATASOURCE_PREFIX = "spring.datasource";
    private static final String REPLICA_DATASOURCE_PREFIX = "spring.replica.datasource";

    @Autowired
    private Environment environment;

    /**
     * Provide a Clock instance. This is an external source of time, so effectively a
     * read-only repository.
     * @return
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        final RoutingDataSource routingDataSource = new RoutingDataSource();

        final DataSource primaryDataSource = buildDataSource("PrimaryHikariPool", PRIMARY_DATASOURCE_PREFIX, null);
        final DataSource replicaDataSource = buildDataSource("ReplicaHikariPool", REPLICA_DATASOURCE_PREFIX, PRIMARY_DATASOURCE_PREFIX);

        final var targetDataSources = new HashMap<>();
        targetDataSources.put(RoutingDataSource.Route.PRIMARY, primaryDataSource);
        targetDataSources.put(RoutingDataSource.Route.REPLICA, replicaDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(replicaDataSource);

        return routingDataSource;
    }

    private DataSource buildDataSource(String poolName, String dataSourcePrefix, String dataSourceDefault) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(poolName);

        if (StringUtils.isNotBlank(environment.getProperty(String.format("%s.url", dataSourcePrefix)))) {
            setDsConfig(dataSourcePrefix, hikariConfig);
        } else {
            setDsConfig(dataSourceDefault, hikariConfig);
        }

        return new HikariDataSource(hikariConfig);
    }

    private void setDsConfig(String dataSourcePrefix, final HikariConfig hikariConfig) {
        hikariConfig.setJdbcUrl(environment.getProperty(String.format("%s.url", dataSourcePrefix)));
        hikariConfig.setUsername(environment.getProperty(String.format("%s.username", dataSourcePrefix)));
        hikariConfig.setPassword(environment.getProperty(String.format("%s.password", dataSourcePrefix)));

        String maxPoolSize = environment.getProperty(String.format("%s.hikari.maximum-pool-size", dataSourcePrefix));
        if (StringUtils.isNotBlank(maxPoolSize)) {
            hikariConfig.setMaximumPoolSize(Integer.valueOf(maxPoolSize));
        }
        var connectionTimeout = environment.getProperty(String.format("%s.hikari.connectionTimeout", dataSourcePrefix));
        if (StringUtils.isNotBlank(connectionTimeout)) {
            hikariConfig.setConnectionTimeout(Integer.valueOf(connectionTimeout));
        }
        var validationTimeout = environment.getProperty(String.format("%s.hikari.validationTimeout", dataSourcePrefix));
        if (StringUtils.isNotBlank(validationTimeout)) {
            hikariConfig.setValidationTimeout(Integer.valueOf(validationTimeout));
        }
    }

}
