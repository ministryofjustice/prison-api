package uk.gov.justice.hmpps.prison.web.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.justice.hmpps.prison.values.Currency;

@Configuration
public class AppBeanConfiguration {

    private @Value("${api.currency:GBP}") String currency;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer objectMapperBuilder() {
        return builder -> builder
                .dateFormat(new StdDateFormat())
                .modules(new JavaTimeModule())
                .featuresToDisable(
                        SerializationFeature.INDENT_OUTPUT,
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        SerializationFeature.FAIL_ON_EMPTY_BEANS,
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public Currency currency() {
        return Currency.builder().code(currency).build();
    }
}
