package net.syscon.elite.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;

@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScan(basePackages = { "net.syscon.elite.repository", "net.syscon.util" })
public class PersistenceConfigs {
    /**
     * Provide a Clock instance. This is an external source of time, so effectively a
     * read-only repository.
     * @return
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
