package net.syscon.elite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "net.syscon", "uk.gov.justice.hmpps" })
@EnableJpaRepositories(basePackages = { "net.syscon", "uk.gov.justice.hmpps" })
@EntityScan(basePackages = { "net.syscon", "uk.gov.justice.hmpps" })
@EnableScheduling
public class Elite2ApiServer {
    public static void main(final String[] args) {
        SpringApplication.run(Elite2ApiServer.class, args);
    }
}
