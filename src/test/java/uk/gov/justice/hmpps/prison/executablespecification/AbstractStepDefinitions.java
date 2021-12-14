package uk.gov.justice.hmpps.prison.executablespecification;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AddIepLevelSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AdjudicationSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AgencySteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingActivitySteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingAlertSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingAliasSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingAppointmentSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingAssessmentSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingDetailSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingEventSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingIEPSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingSentenceDetailSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingVisitSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BulkAppointmentSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.CaseNoteSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.ContactSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.CurfewSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.FinanceSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.KeyWorkerAllocatedOffendersSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.KeyWorkerAllocationSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.KeyWorkerSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.LocationsSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.MovementsSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.MyAssignmentsSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.NomisApiV1Steps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.OffenderAdjudicationSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.OffenderIdentifierSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.OffenderSearchSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.OffenderSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.PersonIdentifierSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.PrisonContactDetailsSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.PrisonerSearchSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.ReferenceDomainsSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.SchedulesSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.StaffSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.UserSteps;
import uk.gov.justice.hmpps.prison.test.DatasourceActiveProfilesResolver;
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper;

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
