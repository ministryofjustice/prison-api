package net.syscon.elite.executablespecification;

import net.syscon.elite.api.support.Order;
import net.syscon.elite.executablespecification.steps.AccessRoleSteps;
import net.syscon.elite.executablespecification.steps.AddIepLevelSteps;
import net.syscon.elite.executablespecification.steps.AdjudicationSteps;
import net.syscon.elite.executablespecification.steps.AgencySteps;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import net.syscon.elite.executablespecification.steps.BookingActivitySteps;
import net.syscon.elite.executablespecification.steps.BookingAlertSteps;
import net.syscon.elite.executablespecification.steps.BookingAliasSteps;
import net.syscon.elite.executablespecification.steps.BookingAppointmentSteps;
import net.syscon.elite.executablespecification.steps.BookingAssessmentSteps;
import net.syscon.elite.executablespecification.steps.BookingDetailSteps;
import net.syscon.elite.executablespecification.steps.BookingEventSteps;
import net.syscon.elite.executablespecification.steps.BookingIEPSteps;
import net.syscon.elite.executablespecification.steps.BookingSearchSteps;
import net.syscon.elite.executablespecification.steps.BookingSentenceDetailSteps;
import net.syscon.elite.executablespecification.steps.BookingVisitSteps;
import net.syscon.elite.executablespecification.steps.BulkAppointmentSteps;
import net.syscon.elite.executablespecification.steps.CaseNoteSteps;
import net.syscon.elite.executablespecification.steps.ContactSteps;
import net.syscon.elite.executablespecification.steps.CurfewSteps;
import net.syscon.elite.executablespecification.steps.FinanceSteps;
import net.syscon.elite.executablespecification.steps.KeyWorkerAllocatedOffendersSteps;
import net.syscon.elite.executablespecification.steps.KeyWorkerAllocationSteps;
import net.syscon.elite.executablespecification.steps.KeyWorkerSteps;
import net.syscon.elite.executablespecification.steps.LocationsSteps;
import net.syscon.elite.executablespecification.steps.MovementsSteps;
import net.syscon.elite.executablespecification.steps.MyAssignmentsSteps;
import net.syscon.elite.executablespecification.steps.NomisApiV1Steps;
import net.syscon.elite.executablespecification.steps.OffenderAdjudicationSteps;
import net.syscon.elite.executablespecification.steps.OffenderIdentifierSteps;
import net.syscon.elite.executablespecification.steps.OffenderSearchSteps;
import net.syscon.elite.executablespecification.steps.OffenderSteps;
import net.syscon.elite.executablespecification.steps.PersonIdentifierSteps;
import net.syscon.elite.executablespecification.steps.PrisonContactDetailsSteps;
import net.syscon.elite.executablespecification.steps.PrisonerSearchSteps;
import net.syscon.elite.executablespecification.steps.ReferenceDomainsSteps;
import net.syscon.elite.executablespecification.steps.SchedulesSteps;
import net.syscon.elite.executablespecification.steps.StaffSteps;
import net.syscon.elite.executablespecification.steps.UserSteps;
import net.syscon.elite.test.DatasourceActiveProfilesResolver;
import net.syscon.elite.util.JwtAuthenticationHelper;
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
@TestPropertySource({"/application-test.properties"})
abstract class AbstractStepDefinitions {
    @TestConfiguration
    static class Config {

        @Bean
        public AccessRoleSteps accessRoleSteps() {
            return new AccessRoleSteps();
        }

        @Bean
        public AuthTokenHelper auth(final JwtAuthenticationHelper jwtAuthenticationHelper) {
            return new AuthTokenHelper(jwtAuthenticationHelper);
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
        public OffenderSteps offenderSteps() {
            return new OffenderSteps();
        }

        @Bean
        public OffenderAdjudicationSteps offenderAdjudicationSteps() {
            return new OffenderAdjudicationSteps();
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
        public OffenderIdentifierSteps offenderIdentifierSteps() {
            return new OffenderIdentifierSteps();
        }

        @Bean
        public CurfewSteps curfewSteps() {
            return new CurfewSteps();
        }

        @Bean
        public BulkAppointmentSteps bulkAppointmentSteps() {
            return new BulkAppointmentSteps();
        }

        @Bean
        public AddIepLevelSteps addIepLevelSteps() {
            return new AddIepLevelSteps();
        }

        @Bean
        public NomisApiV1Steps nomisApiV1Steps() {
            return new NomisApiV1Steps();
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
