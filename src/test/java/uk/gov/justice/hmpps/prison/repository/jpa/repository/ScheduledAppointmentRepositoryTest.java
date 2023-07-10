package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ScheduledAppointment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class ScheduledAppointmentRepositoryTest {

    @Autowired
    private ScheduledAppointmentRepository scheduledAppointmentRepository;

    @Test
    public void returnScheduledAppointmentsByAgencyLocationAndDate() {
        final var date = LocalDate.of(2017, 1, 2);
        final var appointments = scheduledAppointmentRepository.findByAgencyIdAndEventDateAndLocationId("LEI", date, -28L);

        final var scheduledAppointment = ScheduledAppointment
                .builder()
                .eventId(-31L)
                .offenderNo("A1234AB")
                .firstName("GILLIAN")
                .lastName("ANDERSON")
                .eventDate(date)
                .startTime(LocalDateTime.parse("2017-01-02T19:30"))
                .endTime(LocalDateTime.parse("2017-01-02T20:30"))
                .appointmentTypeDescription("Medical - Dentist")
                .appointmentTypeCode("MEDE")
                .locationDescription("Visiting Room")
                .locationId(-28L)
                .agencyId("LEI")
                .createUserId("SA")
                .build();

        assertThat(appointments).containsOnly(scheduledAppointment);
    }

    @Test
    public void returnScheduledAppointmentsByAgencyAndDate() {
        final var date = LocalDate.of(2017, 1, 2);
        final var appointments = scheduledAppointmentRepository.findByAgencyIdAndEventDate("LEI", date);

        assertThat(appointments).containsExactly(
                ScheduledAppointment
                        .builder()
                        .eventId(-31L)
                        .offenderNo("A1234AB")
                        .firstName("GILLIAN")
                        .lastName("ANDERSON")
                        .eventDate(date)
                        .startTime(LocalDateTime.parse("2017-01-02T18:00"))
                        .endTime(LocalDateTime.parse("2017-01-02T18:30"))
                        .appointmentTypeDescription("Medical - Dentist")
                        .appointmentTypeCode("MEDE")
                        .locationDescription("Medical Centre")
                        .locationId(-29L)
                        .agencyId("LEI")
                        .createUserId("SA")
                        .build(),
                ScheduledAppointment
                        .builder()
                        .eventId(-31L)
                        .offenderNo("A1234AB")
                        .firstName("GILLIAN")
                        .lastName("ANDERSON")
                        .eventDate(date)
                        .startTime(LocalDateTime.parse("2017-01-02T19:30"))
                        .endTime(LocalDateTime.parse("2017-01-02T20:30"))
                        .appointmentTypeDescription("Medical - Dentist")
                        .appointmentTypeCode("MEDE")
                        .locationDescription("Visiting Room")
                        .locationId(-28L)
                        .agencyId("LEI")
                        .createUserId("SA")
                        .build(),
                ScheduledAppointment
                        .builder()
                        .eventId(-33L)
                        .offenderNo("A1234AB")
                        .firstName("GILLIAN")
                        .lastName("ANDERSON")
                        .eventDate(date)
                        .startTime(LocalDateTime.parse("2017-01-02T19:30"))
                        .endTime(LocalDateTime.parse("2017-01-02T20:30"))
                        .appointmentTypeDescription("Medical - Dentist")
                        .appointmentTypeCode("MEDE")
                        .locationDescription("Visiting Room")
                        .locationId(-28L)
                        .agencyId("LEI")
                        .createUserId("SA")
                        .build()
        );
    }

}
