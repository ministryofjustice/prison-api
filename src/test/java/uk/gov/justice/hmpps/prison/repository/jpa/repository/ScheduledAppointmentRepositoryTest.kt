package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.ScheduledAppointment
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ScheduledAppointmentRepositoryTest(
  @Autowired private val scheduledAppointmentRepository: ScheduledAppointmentRepository,
) {
  @Test
  fun returnScheduledAppointmentsByAgencyLocationAndDate() {
    val date = LocalDate.of(2017, 1, 2)
    val appointments = scheduledAppointmentRepository.findByAgencyIdAndEventDateAndLocationId("LEI", date, -28L)

    assertThat(appointments).containsExactly(
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
        .eventStatus("SCH")
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
        .eventStatus("CANC")
        .build(),
    )
  }

  @Test
  fun returnScheduledAppointmentsByAgencyAndDate() {
    val date = LocalDate.of(2017, 1, 2)
    val appointments = scheduledAppointmentRepository.findByAgencyIdAndEventDate("LEI", date)

    assertThat(appointments).containsExactly(
      ScheduledAppointment
        .builder()
        .eventId(-30L)
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
        .eventStatus("SCH")
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
        .eventStatus("SCH")
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
        .eventStatus("CANC")
        .build(),
    )
  }
}
