package uk.gov.justice.hmpps.prison;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"uk.gov.justice.hmpps.prison" })
@EnableJpaRepositories(basePackages = {"uk.gov.justice.hmpps.prison" })
@EntityScan(basePackages = { "uk.gov.justice.hmpps.prison"})
@EnableScheduling
public class PrisonApiServer {
    public static void main(final String[] args) {
        SpringApplication.run(PrisonApiServer.class, args);
    }
}
