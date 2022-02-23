package uk.gov.justice.hmpps.prison;

import org.springdoc.hateoas.SpringDocHateoasConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = {"uk.gov.justice.hmpps"}, exclude = SpringDocHateoasConfiguration.class)
@EntityScan(basePackages = {"uk.gov.justice.hmpps"})
@EnableScheduling
public class PrisonApiServer {
    public static void main(final String[] args) {
        SpringApplication.run(PrisonApiServer.class, args);
    }
}
