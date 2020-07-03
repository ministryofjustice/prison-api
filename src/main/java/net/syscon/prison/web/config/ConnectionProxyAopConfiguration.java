package net.syscon.prison.web.config;

import net.syscon.prison.aop.connectionproxy.OracleConnectionAspect;
import net.syscon.prison.aop.connectionproxy.RoleConfigurer;
import net.syscon.prison.aop.connectionproxy.RolePasswordSupplier;
import net.syscon.prison.security.AuthenticationFacade;
import net.syscon.util.SQLProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


@Profile("connection-proxy")

@Configuration()
@EnableAspectJAutoProxy
public class ConnectionProxyAopConfiguration {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${oracle.default.schema}")
    private String defaultSchema;

    @Value("${oracle.tag.role.name}")
    private String tagUser;

    @Bean
    public OracleConnectionAspect oracleProxyConnectionAspect(
            final AuthenticationFacade authenticationFacade,
            final RoleConfigurer roleConfigurer
    ) {
        return new OracleConnectionAspect(authenticationFacade, roleConfigurer, defaultSchema);
    }

    @Bean
    public RoleConfigurer roleConfigurer(final RolePasswordSupplier rolePasswordSupplier) {
        return new RoleConfigurer(tagUser, rolePasswordSupplier);
    }

    @Bean
    public RolePasswordSupplier rolePasswordSupplier(final SQLProvider sqlProvider) {
        return new RolePasswordSupplier(
                sqlProvider,
                new NamedParameterJdbcTemplate(
                        new DriverManagerDataSource(jdbcUrl, username, password)
                ),
                defaultSchema);
    }
}
