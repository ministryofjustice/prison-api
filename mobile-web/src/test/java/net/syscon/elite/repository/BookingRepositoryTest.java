package net.syscon.elite.repository;

import static org.junit.Assert.assertEquals;
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
}
