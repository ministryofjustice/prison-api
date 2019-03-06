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
import org.springframework.test.context.TestPropertySource;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Abstract base class for Serenity/Cucumber BDD step definitions.
 */
@ActiveProfiles(resolver = DatasourceActiveProfilesResolver.class)
@SuppressWarnings("SpringJavaAutowiringInspection")
@ContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource({ "/application-test.properties" })
abstract class AbstractStepDefinitions {
    @TestConfiguration
    static class Config {


        @Bean
        public AccessRoleSteps accessRoleSteps() {
            return new AccessRoleSteps();
        }

        @Bean
        public AuthenticationSteps auth() {
            return new AuthenticationSteps();
        }

        @Bean
        public UserSteps user() {
            return new UserSteps();
        }

        @Bean
        public AgencySteps agency() {
            return new AgencySteps();
        }

        @Bean
        public CaseNoteSteps caseNote() {
            return new CaseNoteSteps();
        }

        @Bean
        public MovementsSteps movement() {
            return new MovementsSteps();
        }

        @Bean
        public BookingSearchSteps bookingSearch() {
            return new BookingSearchSteps();
        }

        @Bean
        public LocationsSteps location() {
            return new LocationsSteps();
        }

        @Bean
        public BookingAliasSteps bookingAlias() {
            return new BookingAliasSteps();
        }

        @Bean
        public BookingDetailSteps bookingDetail() {
            return new BookingDetailSteps();
        }

        @Bean
        public BookingSentenceDetailSteps bookingSentenceDetail() {
            return new BookingSentenceDetailSteps();
        }

        @Bean
        public BookingIEPSteps bookingIEP() {
            return new BookingIEPSteps();
        }

        @Bean
        public BookingActivitySteps bookingActivity() {
            return new BookingActivitySteps();
        }

        @Bean
        public BookingAlertSteps bookingAlert() {
            return new BookingAlertSteps();
        }

        @Bean
        public OffenderSearchSteps offenderSearch() {
            return new OffenderSearchSteps();
        }

        @Bean
        public PrisonerSearchSteps prisonerSearch() {
            return new PrisonerSearchSteps();
        }

        @Bean
        public ReferenceDomainsSteps referenceDomain() {
            return new ReferenceDomainsSteps();
        }

        @Bean
        public MyAssignmentsSteps userAssignment() {
            return new MyAssignmentsSteps();
        }

        @Bean
        public FinanceSteps bookingFinance() {
            return new FinanceSteps();
        }

        @Bean
        public BookingSentenceSteps bookingSentence() {
            return new BookingSentenceSteps();
        }

        @Bean
        public ContactSteps bookingContact() {
            return new ContactSteps();
        }

        @Bean
        public AdjudicationSteps bookingAdjudication() {
            return new AdjudicationSteps();
        }

        @Bean
        public BookingAssessmentSteps bookingAssessment() {
            return new BookingAssessmentSteps();
        }

        @Bean
        public BookingVisitSteps bookingVisit() {
            return new BookingVisitSteps();
        }
        
        @Bean
        public BookingEventSteps bookingEvent() {
            return new BookingEventSteps();
        }

        @Bean
        public BookingAppointmentSteps bookingAppointment() {
            return new BookingAppointmentSteps();
        }

        @Bean
        public SchedulesSteps schedules() {
            return new SchedulesSteps();
        }

        @Bean
        public PrisonContactDetailsSteps prison() {
            return new PrisonContactDetailsSteps();
        }

        @Bean
        public KeyWorkerAllocationSteps keyWorkerAllocation() {
            return new KeyWorkerAllocationSteps();
        }

        @Bean
        public KeyWorkerAllocatedOffendersSteps keyWorkerAllocatedOffenders() {
            return new KeyWorkerAllocatedOffendersSteps();
        }

        @Bean
        public KeyWorkerSteps keyWorker() {
            return new KeyWorkerSteps();
        }

        @Bean
        public StaffSteps staff() {
            return new StaffSteps();
        }

        @Bean
        public PersonIdentifierSteps personIdentifierSteps() {
            return new PersonIdentifierSteps();
        }

        @Bean
        public CurfewSteps curfewSteps() { return new CurfewSteps(); }

        @Bean
        public BulkAppointmentSteps bulkAppointmentSteps() {
            return new BulkAppointmentSteps();
        }
    }

    int ord2idx(final String ordinal) {
        final var numberOnly = StringUtils.trimToEmpty(ordinal).replaceAll("[^0-9]", "");
        int index;

        try {
            index = Integer.parseInt(numberOnly) - 1;
        } catch (final NumberFormatException ex) {
            index = -1;
        }

        return index;
    }

    Order parseSortOrder(final String sortOrder) {
        final Order order;

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
