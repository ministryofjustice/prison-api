package net.syscon.elite.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.web.config.PersistenceConfigs;

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
import java.util.List;

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
        final List<PrisonerSchedule> results = repository.getLocationActivities(-26L, date, toDate, null, null);
        assertThat(results).hasSize(14);
        assertPrisonerDetails(results.get(0));
        // assert at least 1 field from all results
        assertThat(results.get(1).getStartTime().toString()).isEqualTo("2017-09-12T13:00");
        assertThat(results.get(2).getStartTime().toString()).isEqualTo("2017-09-13T13:00");
        assertThat(results.get(3).getStartTime().toString()).isEqualTo("2017-09-14T13:00");
        assertThat(results.get(4).getStartTime().toString()).isEqualTo("2017-09-15T13:00");
        assertThat(results.get(5).getLastName()).isEqualTo("ANDERSON"); // date today
        assertThat(results.get(6).getLastName()).isEqualTo("ANDERSON");
        assertThat(results.get(7).getStartTime().toString()).isEqualTo("2017-09-11T13:00");
        assertThat(results.get(8).getStartTime().toString()).isEqualTo("2017-09-12T13:00");
        assertThat(results.get(9).getStartTime().toString()).isEqualTo("2017-09-13T13:00");
        assertThat(results.get(10).getStartTime().toString()).isEqualTo("2017-09-14T13:00");
        assertThat(results.get(11).getStartTime().toString()).isEqualTo("2017-09-15T13:00");
        assertThat(results.get(12).getLastName()).isEqualTo("BATES");
        assertThat(results.get(13).getLastName()).isEqualTo("BATES");
    }

    @Test
    public void testGetLocationAppointments() {
        final LocalDate date = LocalDate.parse("2015-12-11");
        final LocalDate toDate = LocalDate.now();
          final List<PrisonerSchedule> results =  repository.getLocationAppointments(-28L, date, toDate, null, null);
          assertThat(results).hasSize(3);
          assertThat(results.get(0).getLastName()).isEqualTo("ANDERSON");
          assertThat(results.get(1).getLastName()).isEqualTo("BATES");
          assertThat(results.get(2).getLastName()).isEqualTo("DUCK");
    }

    @Test
    public void testGetLocationVisits() {
        final LocalDate date = LocalDate.parse("2015-12-11");
        final LocalDate toDate = LocalDate.now();
          final List<PrisonerSchedule> results =  repository.getLocationVisits(-25L, date, toDate, null, null);
          assertThat(results).hasSize(5);
          assertThat(results.get(0).getLastName()).isEqualTo("ANDERSON");
          assertThat(results.get(1).getLastName()).isEqualTo("ANDERSON");
          assertThat(results.get(2).getLastName()).isEqualTo("ANDERSON");
          assertThat(results.get(3).getLastName()).isEqualTo("ANDERSON");
          assertThat(results.get(4).getLastName()).isEqualTo("BATES");
          /*
<[class PrisonerSchedule {
  offenderNo: A1234AA
  firstName: ARTHUR
  lastName: ANDERSON
  cellLocation: LEI-A-1-1
  event: VISIT
  eventDescription: Visits
  comment: Official Visit
  startTime: 2017-09-15T14:00
  endTime: 2017-09-15T16:00
},
    class PrisonerSchedule {
  offenderNo: A1234AA
  firstName: ARTHUR
  lastName: ANDERSON
  cellLocation: LEI-A-1-1
  event: VISIT
  eventDescription: Visits
  comment: Official Visit
  startTime: 2017-05-10T14:30
  endTime: 2017-05-10T16:30
},
    class PrisonerSchedule {
  offenderNo: A1234AA
  firstName: ARTHUR
  lastName: ANDERSON
  cellLocation: LEI-A-1-1
  event: VISIT
  eventDescription: Visits
  comment: Official Visit
  startTime: 2017-03-10T14:30
  endTime: 2017-03-10T16:30
},
    class PrisonerSchedule {
  offenderNo: A1234AB
  firstName: GILLIAN
  lastName: ANDERSON
  cellLocation: LEI-H-1-5
  event: VISIT
  eventDescription: Visits
  comment: Official Visit
  startTime: 2017-10-10T10:00
  endTime: 2017-10-10T12:00
}
,
    class PrisonerSchedule {
  offenderNo: A1234AC
  firstName: NORMAN
  lastName: BATES
  cellLocation: LEI-A-1-1
  event: VISIT
  eventDescription: Visits
  comment: Official Visit
  startTime: 2018-01-04T00:00
  endTime: 2018-01-04T00:00
}
]>
           */
//        assertEquals("2017-11-13T14:30", visit.getStartTime().toString());
//        assertEquals("2017-11-13T15:30", visit.getEndTime().toString());
//        assertNull(visit.getLeadVisitor());
//        assertNull(visit.getRelationship());
    }

}
