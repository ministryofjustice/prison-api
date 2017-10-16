package net.syscon.elite.executableSpecification;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import net.syscon.elite.executableSpecification.steps.AuthenticationSteps;
import net.syscon.elite.executableSpecification.steps.BookingAliasSteps;
import net.syscon.elite.executableSpecification.steps.BookingDetailSteps;
import net.syscon.elite.executableSpecification.steps.BookingIEPSteps;
import net.syscon.elite.executableSpecification.steps.BookingSearchSteps;
import net.syscon.elite.executableSpecification.steps.BookingSentenceDetailSteps;
import net.syscon.elite.executableSpecification.steps.CaseNoteSteps;
import net.syscon.elite.executableSpecification.steps.FinanceSteps;
import net.syscon.elite.executableSpecification.steps.LocationsSteps;
import net.syscon.elite.executableSpecification.steps.MyAssignmentsSteps;
import net.syscon.elite.executableSpecification.steps.OffenderSearchSteps;
import net.syscon.elite.executableSpecification.steps.PrisonerSearchSteps;
import net.syscon.elite.executableSpecification.steps.ReferenceDomainsSteps;
import net.syscon.elite.executableSpecification.steps.UserSteps;
import net.syscon.elite.test.DatasourceActiveProfilesResolver;

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
        BookingDetailSteps bookingDetail() {
            return new BookingDetailSteps();
        }

        @Bean
        BookingSentenceDetailSteps bookingSentenceDetail() {
            return new BookingSentenceDetailSteps();
        }

        @Bean
        BookingIEPSteps bookingIEPSteps() {
            return new BookingIEPSteps();
        }

        @Bean
        OffenderSearchSteps offenderSearch() {
            return new OffenderSearchSteps();
        }

        @Bean
        PrisonerSearchSteps prisonerSearch() {
            return new PrisonerSearchSteps();
        }

        @Bean
        ReferenceDomainsSteps referenceDomainsSteps() {
            return new ReferenceDomainsSteps();
        }

        @Bean
        MyAssignmentsSteps myAssignments() {
            return new MyAssignmentsSteps();
        }

        @Bean
        FinanceSteps financeSteps() {
            return new FinanceSteps();
        }
    }
}
