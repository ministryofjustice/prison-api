package uk.gov.justice.hmpps.prison.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.justice.hmpps.prison.values.Currency;

@Configuration
public class AppBeanConfiguration {

    private @Value("${api.currency:GBP}") String currency;

    @Bean
    public Currency currency() {
        return Currency.builder().code(currency).build();
    }
}
