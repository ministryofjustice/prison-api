package net.syscon.elite.repository.jpa.repository;

import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
public class ScheduleAppointmentRepositoryTest {

    @Autowired
    private ScheduledAppointmentRepository scheduledAppointmentRepository;

    @Test
    public void returnScheduledAppointmentsByAgencyLocationAndDate() {
        final var date = LocalDate.of(2017, 1, 2);
        final var appointments = scheduledAppointmentRepository.findByAgencyIdAndEventDateAndLocationId("LEI", date, -28L);

        assertThat(appointments)
                .extracting(
                        "offenderNo", "firstName", "lastName", "eventDate", "startTime", "endTime",
                        "appointmentTypeDescription", "appointmentTypeCode", "locationDescription", "locationId", "auditUserId", "agencyId"
                ).containsExactly(Tuple.tuple("A1234AB", "GILLIAN", "ANDERSON", date, getLocalDateTime(2017,1,2,19,30), getLocalDateTime(2017,1,2,20,30), "Medical - Dentist", "MEDE", "Visiting Room", -28L, "staff 2", "LEI"));
    }

    @Test
    public void returnScheduledAppointmentsByAgencyAndDate() {
        final var date = LocalDate.of(2017, 1, 2);

        final var appointments = scheduledAppointmentRepository.findByAgencyIdAndEventDate("LEI", date);

        assertThat(appointments)
                .extracting(
                        "offenderNo", "firstName", "lastName", "eventDate", "startTime", "endTime",
                        "appointmentTypeDescription", "appointmentTypeCode", "locationDescription", "locationId", "auditUserId", "agencyId"
                ).containsExactly(
                Tuple.tuple("A1234AA", "ARTHUR", "ANDERSON", date, getLocalDateTime(2017, 1, 2, 18, 0), getLocalDateTime(2017, 1, 2, 18, 30), "Medical - Dentist", "MEDE", "Medical Centre", -29L, "staff 1", "LEI"),
                Tuple.tuple("A1234AB", "GILLIAN", "ANDERSON", date, getLocalDateTime(2017, 1, 2, 19, 30), getLocalDateTime(2017, 1, 2, 20, 30), "Medical - Dentist", "MEDE", "Visiting Room", -28L, "staff 2", "LEI"));
    }

    private LocalDateTime getLocalDateTime(final int year, final int month, final int day, final int hour, final int minute) {
        return LocalDateTime.now()
                .withYear(year)
                .withMonth(month)
                .withDayOfMonth(day)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0);
    }
}
