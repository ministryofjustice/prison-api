package net.syscon.elite.web.config;

import net.syscon.elite.aop.LoggingAspect;
import net.syscon.elite.aop.OracleConnectionAspect;
import net.syscon.util.SQLProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@EnableAspectJAutoProxy
public class AopConfigs {

    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    @Bean
	@Profile("!noproxy")
	public OracleConnectionAspect oracleProxyConnectionAspect(SQLProvider sqlProvider,
                                                              @Value("${app.url}") String jdbcUrl,
                                                              @Value("${app.username}") String username,
                                                              @Value("${app.password}") String password
                                                              ) {
        final DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource(jdbcUrl, username, password);
        final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(driverManagerDataSource);
        final String sql = sqlProvider.get("FIND_ROLE_PASSWORD");
        final MapSqlParameterSource params = new MapSqlParameterSource();
        final String encryptedPassword = jdbcTemplate.queryForObject(sql, params, String.class);
        params.addValue("password", encryptedPassword);
        String rolePassword = jdbcTemplate.queryForObject("SELECT decryption('2DECRYPTPASSWRD', :password) FROM DUAL", params, String.class);
        return new OracleConnectionAspect(rolePassword);
	}

}
