package uk.gov.justice.hmpps.prison.repository;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository repository;

    @BeforeEach
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    private static void assertPrisonerDetails(final PrisonerSchedule details) {
        assertThat(details.getStartTime().toString()).isEqualTo("2017-09-11T13:00");
        assertThat(details.getEndTime().toString()).isEqualTo("2017-09-11T15:00");

        assertThat(details.getComment()).isEqualTo("Woodwork");
        assertThat(details.getEvent()).isEqualTo("EDUC");
        assertThat(details.getEventDescription()).isEqualTo("Education");
        assertThat(details.getOffenderNo()).isEqualTo("A1234AB");
        assertThat(details.getFirstName()).isEqualTo("GILLIAN");
        assertThat(details.getLastName()).isEqualTo("ANDERSON");
        assertThat(details.getCellLocation()).isEqualTo("LEI-H-1-5");
    }

    @Test
    public void testGetLocationActivities() {
        final var date = LocalDate.parse("2015-12-11");
        final var toDate = LocalDate.now();
        final var results = repository.getActivitiesAtLocation(-26L, date, toDate, "lastName,startTime", Order.ASC, false);
        assertThat(results).hasSize(32);
        assertPrisonerDetails(results.get(0));
        // assert at least 1 field from all results
        assertThat(results.get(1).getStartTime().toString()).isEqualTo("2017-09-12T13:00");
        assertThat(results.get(2).getStartTime().toString()).isEqualTo("2017-09-13T13:00");
        assertThat(results.get(3).getStartTime().toString()).isEqualTo("2017-09-14T13:00");
        assertThat(results.get(4).getStartTime().toString()).isEqualTo("2017-09-15T13:00");

        assertThat(results.get(1).getBookingId().toString()).isEqualTo("-2");
        assertThat(results.get(12).getBookingId().toString()).isEqualTo("-3");
        assertThat(results.get(16).getBookingId().toString()).isEqualTo("-4");
        assertThat(results.get(23).getBookingId().toString()).isEqualTo("-4");
        assertThat(results.get(24).getBookingId().toString()).isEqualTo("-5");
        assertThat(results.get(31).getBookingId().toString()).isEqualTo("-5");

        assertThat(results.get(5).getLastName()).isEqualTo("ANDERSON"); // date today
        assertThat(results.get(6).getLastName()).isEqualTo("ANDERSON");

        assertThat(results.get(7).getStartTime().toString()).isEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_DATE) + "T13:00");
        assertThat(results.get(8).getStartTime().toString()).isEqualTo("2017-09-11T13:00");
        assertThat(results.get(9).getStartTime().toString()).isEqualTo("2017-09-12T13:00");
        assertThat(results.get(10).getStartTime().toString()).isEqualTo("2017-09-13T13:00");
        assertThat(results.get(11).getStartTime().toString()).isEqualTo("2017-09-14T13:00");

        assertThat(results.get(12).getLastName()).isEqualTo("BATES");
        assertThat(results.get(13).getLastName()).isEqualTo("BATES");
        assertThat(results.get(14).getLastName()).isEqualTo("BATES");
        assertThat(results.get(15).getLastName()).isEqualTo("BATES");
        assertThat(results.get(16).getLastName()).isEqualTo("CHAPLIN");
        assertThat(results.get(17).getLastName()).isEqualTo("CHAPLIN");
        assertThat(results.get(18).getLastName()).isEqualTo("CHAPLIN");
        assertThat(results.get(19).getLastName()).isEqualTo("CHAPLIN");
        assertThat(results.get(20).getLastName()).isEqualTo("CHAPLIN");
        assertThat(results.get(21).getLastName()).isEqualTo("CHAPLIN");
        assertThat(results.get(22).getLastName()).isEqualTo("CHAPLIN");
        assertThat(results.get(23).getLastName()).isEqualTo("CHAPLIN");
        assertThat(results.get(24).getLastName()).isEqualTo("MATTHEWS");
        assertThat(results.get(25).getLastName()).isEqualTo("MATTHEWS");
        assertThat(results.get(26).getLastName()).isEqualTo("MATTHEWS");
        assertThat(results.get(27).getLastName()).isEqualTo("MATTHEWS");
        assertThat(results.get(28).getLastName()).isEqualTo("MATTHEWS");
        assertThat(results.get(29).getLastName()).isEqualTo("MATTHEWS");
        assertThat(results.get(30).getLastName()).isEqualTo("MATTHEWS");
        assertThat(results.get(31).getLastName()).isEqualTo("MATTHEWS");


        results.forEach(result -> assertThat(result.getLocationId()).isEqualTo(-26L));

    }

    @Test
    public void testGetLocationAppointments() {
        final var date = LocalDate.parse("2015-12-11");
        final var toDate = LocalDate.now();
        final var results = repository.getLocationAppointments(-28L, date, toDate, null, null);
        assertThat(results).hasSize(5);
        assertThat(results.get(0).getLastName()).isEqualTo("ANDERSON");
        assertThat(results.get(1).getLastName()).isEqualTo("BATES");
        assertThat(results.get(2).getLastName()).isEqualTo("MATTHEWS");
        assertThat(results.get(3).getLastName()).isEqualTo("MATTHEWS");
        assertThat(results.get(0).getBookingId().toString()).isEqualTo("-2");
        assertThat(results.get(1).getBookingId().toString()).isEqualTo("-3");
        assertThat(results.get(2).getBookingId().toString()).isEqualTo("-5");

        results.forEach(result -> assertThat(result.getLocationId()).isEqualTo(-28L));
    }

    @Test
    public void testGetLocationVisits() {
        final var date = LocalDate.parse("2015-12-11");
        final var toDate = LocalDate.now();
        final var results = repository.getLocationVisits(-25L, date, toDate, null, null);
        assertThat(results).hasSize(6);
        assertThat(results.get(0).getLastName()).isEqualTo("ANDERSON");
        assertThat(results.get(1).getLastName()).isEqualTo("ANDERSON");
        assertThat(results.get(2).getLastName()).isEqualTo("ANDERSON");
        assertThat(results.get(3).getLastName()).isEqualTo("ANDERSON");
        assertThat(results.get(4).getLastName()).isEqualTo("BATES");
        assertThat(results.get(5).getLastName()).isEqualTo("MATTHEWS");
        assertThat(results.get(0).getBookingId().toString()).isEqualTo("-1");
        assertThat(results.get(4).getBookingId().toString()).isEqualTo("-3");
        assertThat(results.get(5).getBookingId().toString()).isEqualTo("-5");

        results.forEach(result -> assertThat(result.getLocationId()).isEqualTo(-25L));
    }

    @Test
    public void testGetAppointments() {
        final var date = LocalDate.parse("2017-05-12");
        final var results = repository.getAppointments("LEI", Collections.singletonList("A1234AB"), date);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A1234AB");
        assertThat(results.get(0).getStartTime()).isEqualTo(LocalDateTime.parse("2017-05-12T09:30"));
        assertThat(results.get(0).getEvent()).isEqualTo("IMM");
        assertThat(results.get(0).getLocationId()).isEqualTo(-28L);
        assertThat(results.get(0).getEventLocation()).isEqualTo("Visiting Room");
    }

    @Test
    public void testGetVisits() {
        final var date = LocalDate.parse("2017-09-15");
        final var results = repository.getVisits("LEI", Collections.singletonList("A1234AA"), date);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A1234AA");
        assertThat(results.get(0).getStartTime()).isEqualTo(LocalDateTime.parse("2017-09-15T14:00"));
        assertThat(results.get(0).getLocationId()).isEqualTo(-25L);
        assertThat(results.get(0).getEventLocation()).isEqualTo("Chapel");
    }

    @Test
    public void testGetActivities() {
        final var date = LocalDate.parse("2017-09-15");
        final var results = repository.getActivities("LEI", List.of("A1234AB", "A1234AD"), date);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A1234AB");
        assertThat(results.get(0).getExcluded()).isFalse();
        assertThat(results.get(0).getStartTime()).isEqualTo(LocalDateTime.parse("2017-09-15T13:00"));
        assertThat(results.get(0).getLocationId()).isEqualTo(-26L);
        assertThat(results.get(0).getTimeSlot()).isEqualTo(TimeSlot.PM);
        assertThat(results.get(0).getEventLocation()).isEqualTo("Carpentry Workshop");

        assertThat(results.get(1).getOffenderNo()).isEqualTo("A1234AD");
        assertThat(results.get(1).getExcluded()).isFalse();
        assertThat(results.get(1).getStartTime()).isEqualTo(LocalDateTime.parse("2017-09-15T13:00"));
        assertThat(results.get(1).getLocationId()).isEqualTo(-26L);
        assertThat(results.get(1).getTimeSlot()).isEqualTo(TimeSlot.PM);
        assertThat(results.get(1).getEventLocation()).isEqualTo("Carpentry Workshop");
    }

    @Test
    public void testGetActivitiesExcluded() {
        final var date = LocalDate.parse("2017-09-11");
        final var results = repository.getActivities("LEI", Collections.singletonList("A1234AE"), date);
        assertThat(results).asList().extracting("offenderNo", "excluded", "locationId", "timeSlot", "startTime")
                .contains(
                        new Tuple("A1234AE", true, -25L, TimeSlot.AM, LocalDateTime.parse("2017-09-11T09:30:00")),
                        new Tuple("A1234AE", false, -26L, TimeSlot.PM, LocalDateTime.parse("2017-09-11T13:00:00")));
    }

    @Test
    public void testGetCourtEvents() {
        final var results = repository.getCourtEvents(List.of("A1234AA", "A1234AB"), LocalDate.parse("2017-02-17"));

        assertThat(results).asList().hasSize(2);
        assertThat(results).asList().extracting("offenderNo", "eventType", "event", "eventDescription", "eventStatus", "startTime").contains(
                new Tuple("A1234AA", "COURT", "PR", "Production of Unsentenced Inmate at Cour", "EXP", LocalDateTime.parse("2017-02-17T17:00:00")),
                new Tuple("A1234AB", "COURT", "PR", "Production of Unsentenced Inmate at Cour", "COMP", LocalDateTime.parse("2017-02-17T18:00:00")));
    }

    @Test
    public void testThatScheduledActivities_FromVariousActivityLocationsAreReturned() {
        final var date = LocalDate.parse("2015-12-11");
        final var toDate = LocalDate.now();
        final var results = repository.getAllActivitiesAtAgency("LEI", date, toDate, "lastName,startTime", Order.ASC, true);

        assertThat(results).extracting("locationId").contains(-25L, -26L, -27L);
    }

    @Test
    public void testGetAllActivitiesAtAgency() {
        final var date = LocalDate.parse("2015-12-11");
        final var toDate = LocalDate.now();
        final var results = repository.getAllActivitiesAtAgency("LEI", date, toDate, "lastName,startTime", Order.ASC, true);
        assertThat(results).hasSize(91);


        results.forEach(result -> {
            // Check activities are returned for expected locations
            assertThat(List.of(-26L, -27L, -25L)).contains(result.getLocationId());

            // Check activities are of the expected types
            assertThat(List.of("CHAP", "EDUC")).contains(result.getEvent());

            // Check the offenders returned have the expected booking ids
            // -35L is someone at a different agency (simulating being transferred)
            // but who was allocated to a program at LEI during the specified time period
            // -40L is someone with a suspended schedule
            assertThat(List.of(-1L, -2L, -3L, -4L, -5L, -6L, -35L, -40L)).contains(result.getBookingId());

            // Get offender cell locations. -1L and -3L share a cell.
            assertThat(List.of("LEI-A-1-1", "LEI-A-1-2", "LEI-H-1-5", "LEI-A-1", "LEI-A-1-10", "MDI-1-1-001")).contains(result.getCellLocation());

            // Assert it return both suspended and not suspended
            assertThat(List.of(true, false)).contains(result.getSuspended());
        });
    }

    @Test
    public void testScheduledActivity_ForAGivenDateRangeAreReturned() {
        final var fromDate = LocalDate.of(2017, 9, 11);
        final var toDate = LocalDate.of(2017, 9, 12);

        final var activities = repository.getAllActivitiesAtAgency("LEI", fromDate, toDate, "lastName,startTime", Order.ASC, false);

        assertThat(Objects.requireNonNull(activities)
                .stream()
                .flatMap(event -> List.of(event.getStartTime(), event.getEndTime()).stream())
                .allMatch(date -> date.toLocalDate().isEqual(fromDate) || date.toLocalDate().isEqual(toDate)))
                .isTrue();
    }

    @Test
    public void testSuspendedAre_NotReturned() {
        final var fromDate = LocalDate.of(1985, 1, 1);
        final var toDate = LocalDate.of(1985, 1, 1);

        final var activities = repository.getActivitiesAtLocation(-27L, fromDate, toDate, "lastName,startTime", Order.ASC, false);

        assertThat(activities).hasSize(0);
    }

    @Test
    public void testSuspendedActivity_Returned() {
        final var fromDate = LocalDate.of(1985, 1, 1);
        final var toDate = LocalDate.of(1985, 1, 1);

        final var activities = repository.getActivitiesAtLocation(-27L, fromDate, toDate, "lastName,startTime", Order.ASC, true);

        assertThat(activities).hasSize(1);
        assertThat(activities).asList().extracting("offenderNo", "event", "eventDescription", "eventStatus", "startTime", "suspended").contains(
                new Tuple("A1234AF", "EDUC", "Education", null, LocalDateTime.parse("1985-01-01T13:10:00"), true));
    }
}
