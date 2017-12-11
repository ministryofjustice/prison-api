package net.syscon.elite.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.ScheduledEvent;
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

import java.time.LocalDateTime;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository repository;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    private static void assertVisitDetails(final ScheduledEvent visit) {
        assertEquals(-1L, visit.getBookingId().longValue());
        assertEquals("2016-12-11", visit.getEventDate().toString());
        assertEquals("2016-12-11T14:30", visit.getStartTime().toString());
        assertEquals("2016-12-11T15:30", visit.getEndTime().toString());
        assertEquals("INT_MOV", visit.getEventClass());
        assertEquals("VISIT", visit.getEventType());
        assertEquals("Visiting Room", visit.getEventLocation());
        assertEquals("SCH", visit.getEventStatus());
        assertEquals("VIS", visit.getEventSource());
        assertEquals("SCON", visit.getEventSourceCode());
    }

    @Test
    public void testCreateBookingAppointment() {
        final NewAppointment appt = NewAppointment.builder()
                .appointmentType("APT_TYPE")
                .locationId(-29L)
                .startTime(LocalDateTime.parse("2017-12-23T10:15:30"))
                .build();

        final Long eventId = repository.createBookingAppointment(-2L, appt, "LEI");

        final ScheduledEvent event = repository.getBookingAppointment(-2L, eventId);
        assertEquals(appt.getAppointmentType(), event.getEventSubType());
        assertEquals("Medical Centre", event.getEventLocation());
        assertEquals(appt.getStartTime(), event.getStartTime());
        assertEquals(appt.getStartTime().toLocalDate(), event.getEventDate());
    }

    @Test
    public void testGetBookingVisitLastSameDay() {
        final ScheduledEvent visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2016-12-11T16:00"));
        assertVisitDetails(visit);
    }

    @Test
    public void testGetBookingVisitLastDifferentDay() {
        final ScheduledEvent visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2016-12-20T00:00"));
        assertVisitDetails(visit);
    }

    @Test
    public void testGetBookingVisitLastMultipleCandidates() {
        final ScheduledEvent visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2017-12-07T00:00"));
        assertEquals(-1L, visit.getBookingId().longValue());
        assertEquals("2017-11-13", visit.getEventDate().toString());
        assertEquals("2017-11-13T14:30", visit.getStartTime().toString());
        assertEquals("2017-11-13T15:30", visit.getEndTime().toString());
    }

    @Test
    public void testGetBookingVisitLastNonexistentBooking() {
        final ScheduledEvent visit = repository.getBookingVisitLast(-99L, LocalDateTime.parse("2016-12-11T16:00:00"));
        assertNull(visit);
    }

    @Test
    public void testGetBookingVisitLastEarlyDate() {
        final ScheduledEvent visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2011-12-11T16:00:00"));
        assertNull(visit);
    }
}
