package net.syscon.elite.executablespecification;

import net.syscon.elite.api.support.Order;
import net.syscon.elite.executablespecification.steps.*;
import net.syscon.elite.test.DatasourceActiveProfilesResolver;
import org.apache.commons.lang3.StringUtils;
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
        BookingActivitySteps bookingActivitySteps() {
            return new BookingActivitySteps();
        }

        @Bean
        BookingAlertSteps bookingAlertSteps() {
            return new BookingAlertSteps();
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

        @Bean
        BookingSentenceSteps bookingSentenceSteps() {
            return new BookingSentenceSteps();
        }
        @Bean
        BookingAssessmentSteps bookingAssessmentSteps() {
            return new BookingAssessmentSteps();
        }
    }

    protected int ord2idx(String ordinal) {
        String numberOnly = StringUtils.trimToEmpty(ordinal).replaceAll("[^0-9]", "");
        int index;

        try {
            index = Integer.parseInt(numberOnly) - 1;
        } catch (NumberFormatException ex) {
            index = -1;
        }

        return index;
    }

    protected Order parseSortOrder(String sortOrder) {
        Order order;

        if (StringUtils.startsWithIgnoreCase(sortOrder, "DESC")) {
            order = Order.DESC;
        } else if (StringUtils.startsWithIgnoreCase(sortOrder, "ASC")) {
            order = Order.ASC;
        } else {
            order = null;
        }

        return order;
    }
}
