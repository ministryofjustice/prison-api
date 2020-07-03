package net.syscon.prison;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "net.syscon", "uk.gov.justice.hmpps" })
@EnableJpaRepositories(basePackages = { "net.syscon", "uk.gov.justice.hmpps" })
@EntityScan(basePackages = { "net.syscon", "uk.gov.justice.hmpps" })
@EnableScheduling
public class PrisonApiServer {
    public static void main(final String[] args) {
        SpringApplication.run(PrisonApiServer.class, args);
    }
}
