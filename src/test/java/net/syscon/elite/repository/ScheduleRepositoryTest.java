package net.syscon.elite.repository;

import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository repository;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    private static void assertPrisonerDetails(final PrisonerSchedule details) {
        assertEquals("2017-09-11T13:00", details.getStartTime().toString());
        assertEquals("2017-09-11T15:00", details.getEndTime().toString());

        assertEquals("Woodwork", details.getComment());
        assertEquals("EDUC", details.getEvent());
        assertEquals("Education", details.getEventDescription());
        assertEquals("A1234AB", details.getOffenderNo());
        assertEquals("GILLIAN", details.getFirstName());
        assertEquals("ANDERSON", details.getLastName());
        assertEquals("LEI-H-1-5", details.getCellLocation());
    }

    @Test
    public void testGetLocationActivities() {
        final LocalDate date = LocalDate.parse("2015-12-11");
        final LocalDate toDate = LocalDate.now();
        final List<PrisonerSchedule> results = repository.getLocationActivities(-26L, date, toDate, "lastName,startTime", Order.ASC);
        assertThat(results).hasSize(24);
        assertPrisonerDetails(results.get(0));
        // assert at least 1 field from all results
        assertThat(results.get(1).getStartTime().toString()).isEqualTo("2017-09-12T13:00");
        assertThat(results.get(2).getStartTime().toString()).isEqualTo("2017-09-13T13:00");
        assertThat(results.get(3).getStartTime().toString()).isEqualTo("2017-09-14T13:00");
        assertThat(results.get(4).getStartTime().toString()).isEqualTo("2017-09-15T13:00");

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
        assertThat(results.get(16).getLastName()).isEqualTo("DUCK");
        assertThat(results.get(17).getLastName()).isEqualTo("DUCK");
        assertThat(results.get(18).getLastName()).isEqualTo("DUCK");
        assertThat(results.get(19).getLastName()).isEqualTo("DUCK");
        assertThat(results.get(20).getLastName()).isEqualTo("DUCK");
        assertThat(results.get(21).getLastName()).isEqualTo("DUCK");
        assertThat(results.get(22).getLastName()).isEqualTo("DUCK");
        assertThat(results.get(23).getLastName()).isEqualTo("DUCK");

        results.forEach(result -> assertThat(result.getLocationId()).isEqualTo(-26L));

    }

    @Test
    public void testGetLocationAppointments() {
        final LocalDate date = LocalDate.parse("2015-12-11");
        final LocalDate toDate = LocalDate.now();
          final List<PrisonerSchedule> results =  repository.getLocationAppointments(-28L, date, toDate, null, null);
          assertThat(results).hasSize(4);
          assertThat(results.get(0).getLastName()).isEqualTo("ANDERSON");
          assertThat(results.get(1).getLastName()).isEqualTo("BATES");
          assertThat(results.get(2).getLastName()).isEqualTo("DUCK");
          assertThat(results.get(3).getLastName()).isEqualTo("DUCK");

          results.forEach(result -> assertThat(result.getLocationId()).isEqualTo(-28L));
    }

    @Test
    public void testGetLocationVisits() {
        final LocalDate date = LocalDate.parse("2015-12-11");
        final LocalDate toDate = LocalDate.now();
          final List<PrisonerSchedule> results =  repository.getLocationVisits(-25L, date, toDate, null, null);
          assertThat(results).hasSize(6);
          assertThat(results.get(0).getLastName()).isEqualTo("ANDERSON");
          assertThat(results.get(1).getLastName()).isEqualTo("ANDERSON");
          assertThat(results.get(2).getLastName()).isEqualTo("ANDERSON");
          assertThat(results.get(3).getLastName()).isEqualTo("ANDERSON");
          assertThat(results.get(4).getLastName()).isEqualTo("BATES");
          assertThat(results.get(5).getLastName()).isEqualTo("DUCK");

        results.forEach(result -> assertThat(result.getLocationId()).isEqualTo(-25L));
    }

    @Test
    public void testGetAppointments() {
        final LocalDate date = LocalDate.parse("2017-05-12");
        final List<PrisonerSchedule> results =  repository.getAppointments("LEI", Collections.singletonList("A1234AB"), date);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A1234AB");
        assertThat(results.get(0).getStartTime()).isEqualTo(LocalDateTime.parse("2017-05-12T09:30"));
        assertThat(results.get(0).getEvent()).isEqualTo("IMM");
        assertThat(results.get(0).getLocationId()).isEqualTo(-28L);
        assertThat(results.get(0).getEventLocation()).isEqualTo("Visiting Room");
    }

    @Test
    public void testGetVisits() {
        final LocalDate date = LocalDate.parse("2017-09-15");
        final List<PrisonerSchedule> results =  repository.getVisits("LEI", Collections.singletonList("A1234AA"), date);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A1234AA");
        assertThat(results.get(0).getStartTime()).isEqualTo(LocalDateTime.parse("2017-09-15T14:00"));
        assertThat(results.get(0).getLocationId()).isEqualTo(-25L);
        assertThat(results.get(0).getEventLocation()).isEqualTo("Chapel");
    }

    @Test
    public void testGetActivities() {
        final LocalDate date = LocalDate.parse("2017-09-15");
        final List<PrisonerSchedule> results =  repository.getActivities("LEI", Collections.singletonList("A1234AB"), date);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A1234AB");
        assertThat(results.get(0).getExcluded()).isFalse();
        assertThat(results.get(0).getStartTime()).isEqualTo(LocalDateTime.parse("2017-09-15T13:00"));
        assertThat(results.get(0).getLocationId()).isEqualTo(-26L);
        assertThat(results.get(0).getTimeSlot()).isEqualTo(TimeSlot.PM);
        assertThat(results.get(0).getEventLocation()).isEqualTo("Carpentry Workshop");
    }

    @Test
    public void testGetActivitiesExcluded() {
        final LocalDate date = LocalDate.parse("2017-09-11");
        final List<PrisonerSchedule> results =  repository.getActivities("LEI", Collections.singletonList("A1234AE"), date);
        assertThat(results).asList().extracting("offenderNo", "excluded", "locationId", "timeSlot", "startTime")
                .contains(
                        new Tuple("A1234AE", true,  -25L, TimeSlot.AM, LocalDateTime.parse("2017-09-11T09:30:00")),
                        new Tuple("A1234AE", false, -26L, TimeSlot.PM, LocalDateTime.parse("2017-09-11T13:00:00")));
    }

    @Test
    public void testGetCourtEvents() {
        List<PrisonerSchedule> results = repository.getCourtEvents(Arrays.asList("A1234AA", "A1234AB"), LocalDate.parse("2017-02-17"));

        assertThat(results).asList().hasSize(2);
        assertThat(results).asList().extracting("offenderNo", "eventType", "event", "eventDescription", "eventStatus", "startTime").contains(
                new Tuple("A1234AA", "COURT", "PR", "Production of Unsentenced Inmate at Cour", "EXP", LocalDateTime.parse("2017-02-17T17:00:00")),
                new Tuple("A1234AB", "COURT", "PR", "Production of Unsentenced Inmate at Cour", "COMP", LocalDateTime.parse("2017-02-17T18:00:00")));
    }
}
