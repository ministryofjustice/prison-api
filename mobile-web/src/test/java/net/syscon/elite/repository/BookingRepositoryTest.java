package net.syscon.elite.repository;

import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.Visit;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

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

    private static void assertVisitDetails(final Visit visit) {
        assertEquals("2016-12-11T14:30", visit.getStartTime().toString());
        assertEquals("2016-12-11T15:30", visit.getEndTime().toString());
        assertEquals("ABS", visit.getEventOutcome());
        assertEquals("Absence", visit.getEventOutcomeDescription());
        assertEquals("JESSY SMITH1", visit.getLeadVisitor());
        assertEquals("UN", visit.getRelationship());
        assertEquals("Uncle", visit.getRelationshipDescription());
        assertEquals("Visiting Room", visit.getLocation());
        assertEquals("CANC", visit.getEventStatus());
        assertEquals("Cancelled", visit.getEventStatusDescription());
        assertEquals("NSHOW", visit.getCancellationReason());
        assertEquals("Visitor Did Not Arrive", visit.getCancellationReasonDescription());
        assertEquals("SCON", visit.getVisitType());
        assertEquals("Social Contact", visit.getVisitTypeDescription());
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
    public void testCreateBookingAppointmentWithEndComment() {
        final NewAppointment appt = NewAppointment.builder()
                .appointmentType("APT_TYPE")
                .locationId(-29L)
                .startTime(LocalDateTime.parse("2017-12-24T10:15:30"))
                .endTime(LocalDateTime.parse("2017-12-24T10:30:00"))
                .comment("Hi there")
                .build();

        final Long eventId = repository.createBookingAppointment(-2L, appt, "LEI");

        final ScheduledEvent event = repository.getBookingAppointment(-2L, eventId);
        assertEquals(appt.getAppointmentType(), event.getEventSubType());
        assertEquals("Medical Centre", event.getEventLocation());
        assertEquals(appt.getStartTime(), event.getStartTime());
        assertEquals(appt.getEndTime(), event.getEndTime());
        assertEquals(appt.getComment(), event.getEventSourceDesc());
        assertEquals(appt.getStartTime().toLocalDate(), event.getEventDate());
    }

    @Test
    public void testGetBookingVisitLastSameDay() {
        final Visit visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2016-12-11T16:00"));
        assertVisitDetails(visit);
    }

    @Test
    public void testGetBookingVisitLastDifferentDay() {
        final Visit visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2016-12-20T00:00"));
        assertVisitDetails(visit);
    }

    @Test
    public void testGetBookingVisitLastMultipleCandidates() {
        final Visit visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2017-12-07T00:00"));
        assertEquals("2017-11-13T14:30", visit.getStartTime().toString());
        assertEquals("2017-11-13T15:30", visit.getEndTime().toString());
        assertNull(visit.getLeadVisitor());
        assertNull(visit.getRelationship());
    }

    @Test
    public void testGetBookingVisitLastNonexistentBooking() {
        final Visit visit = repository.getBookingVisitLast(-99L, LocalDateTime.parse("2016-12-11T16:00:00"));
        assertNull(visit);
    }

    @Test
    public void testGetBookingVisitLastEarlyDate() {
        final Visit visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2011-12-11T16:00:00"));
        assertNull(visit);
    }
}
