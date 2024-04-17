package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule.EventClass
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingAppointmentRepositoryTest {
  @Autowired
  private lateinit var offenderIndividualScheduleRepository: OffenderIndividualScheduleRepository

  @Autowired
  private lateinit var agencyLocationRepository: AgencyLocationRepository

  @Autowired
  private lateinit var agencyInternalLocationRepository: AgencyInternalLocationRepository

  @Autowired
  private lateinit var eventStatusRepository: ReferenceCodeRepository<EventStatus>

  @Autowired
  private lateinit var offenderBookingRepository: OffenderBookingRepository

  private fun createAppointment(
    agencyId: String,
    locationId: Long,
    bookingId: Long,
    appointmentType: String,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
  ): Long {
    val eventStatus = eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED)
      .orElseThrow { RuntimeException("Event status not recognised.") }
    val prison = agencyLocationRepository.findById(agencyId)
      .orElseThrow { RuntimeException("Prison not found") }
    val location = agencyInternalLocationRepository.findById(locationId)
      .orElseThrow { RuntimeException("Location not found") }
    val booking = offenderBookingRepository.findById(bookingId)
      .orElseThrow { RuntimeException("Booking not found") }
    val appointment = OffenderIndividualSchedule()

    appointment.offenderBooking = booking
    appointment.eventClass = EventClass.INT_MOV
    appointment.eventType = "APP"
    appointment.eventStatus = eventStatus
    appointment.eventSubType = appointmentType
    appointment.eventDate = startTime.toLocalDate()
    appointment.startTime = startTime
    appointment.endTime = endTime
    appointment.internalLocation = location
    appointment.fromLocation = prison
    val savedAppointment = offenderIndividualScheduleRepository.save(appointment)
    return savedAppointment.id
  }

  @Test
  fun bookingAppointmentByEventId() {
    val startTime = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS) // Drop nanos.
    val endTime = startTime.plusMinutes(30)
    val bookingId = -30L
    val locationId =
      -28L // LEI_VIS. This should really be a location with location usage 'VIDE' but I don't think it matters for this test.

    val id = createAppointment("LEI", locationId, bookingId, "VLB", startTime, endTime)

    // Could commit and start a new transaction here, but I don't think it is necessary.
    assertThat(offenderIndividualScheduleRepository.findById(id).get())
      .hasFieldOrPropertyWithValue("offenderBooking.bookingId", bookingId)
      .hasFieldOrPropertyWithValue("id", id)
      .hasFieldOrPropertyWithValue("startTime", startTime)
      .hasFieldOrPropertyWithValue("endTime", endTime)
      .hasFieldOrPropertyWithValue("internalLocation.locationId", locationId)
  }

  @Test
  fun deleteBookingAppointment() {
    val startTime = LocalDateTime.now().plusDays(2)
    val endTime = startTime.plusMinutes(30)
    val bookingId = -30L
    val locationId =
      -28L // LEI-LEI_VIS. This should really be a location with location usage 'VIDE' but I don't think it matters for this test.

    val id = createAppointment("LEI", locationId, bookingId, "VLB", startTime, endTime)

    assertThat(offenderIndividualScheduleRepository.findById(id).isPresent).isTrue()

    offenderIndividualScheduleRepository.deleteById(id)

    assertThat(offenderIndividualScheduleRepository.findById(id).isPresent).isFalse()
  }
}
