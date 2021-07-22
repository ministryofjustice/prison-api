package uk.gov.justice.hmpps.prison;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"uk.gov.justice.hmpps"})
public class RepositoryConfiguration {
}
