package net.syscon.elite.executableSpecification;

import net.syscon.elite.executableSpecification.steps.*;
import net.syscon.elite.test.DatasourceActiveProfilesResolver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Abstract base class for Serenity/Cucumber BDD step definitions.
 */
@ActiveProfiles(resolver = DatasourceActiveProfilesResolver.class)
@SuppressWarnings("SpringJavaAutowiringInspection")
@ContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class AbstractStepDefinitions {
    @TestConfiguration
    static class Config {
        @Bean
        AuthenticationSteps auth() {
            return new AuthenticationSteps();
        }

        @Bean
        UserSteps user() {
            return new UserSteps();
        }

        @Bean
        CaseNoteSteps caseNote() {
            return new CaseNoteSteps();
        }

        @Bean
        BookingSearchSteps bookingSearch() {
            return new BookingSearchSteps();
        }

        @Bean
        LocationsSteps location() {
            return new LocationsSteps();
        }

        @Bean
        BookingAliasSteps bookingAlias() {
            return new BookingAliasSteps();
        }

        @Bean
        BookingDetailSteps bookingDetails() {
            return new BookingDetailSteps();
        }
    }
}
