package net.syscon.elite.web.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.HashMap;

@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
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

        final var primaryDataSource = buildDataSource("PrimaryHikariPool", PRIMARY_DATASOURCE_PREFIX, false);
        if (primaryDataSource == null) {
            throw new RuntimeException("No Datasource URL defined");
        }
        final var replicaHikariPool = buildDataSource("ReplicaHikariPool", REPLICA_DATASOURCE_PREFIX, true);
        final var replicaDataSource = replicaHikariPool != null ? replicaHikariPool : primaryDataSource;

        final var targetDataSources = new HashMap<>();
        targetDataSources.put(RoutingDataSource.Route.PRIMARY, primaryDataSource);
        targetDataSources.put(RoutingDataSource.Route.REPLICA, replicaDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);

        return routingDataSource;
    }

    private DataSource buildDataSource(String poolName, String dataSourcePrefix, boolean readonly) {

        String url = environment.getProperty(String.format("%s.url", dataSourcePrefix));
        if (StringUtils.isBlank(url)) {
            return null;
        } else {
            final HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setPoolName(poolName);

            hikariConfig.setJdbcUrl(url);
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

            hikariConfig.setReadOnly(readonly);
            return new HikariDataSource(hikariConfig);
        }
    }

}
