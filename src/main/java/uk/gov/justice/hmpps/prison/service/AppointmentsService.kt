package uk.gov.justice.hmpps.prison.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.validation.Valid
import lombok.extern.slf4j.Slf4j
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import uk.gov.justice.hmpps.prison.api.model.Location
import uk.gov.justice.hmpps.prison.api.model.NewAppointment
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode
import uk.gov.justice.hmpps.prison.api.model.ScheduledAppointmentDto
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.CreatedAppointmentDetails
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.Repeat
import uk.gov.justice.hmpps.prison.api.support.TimeSlot
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule.EventClass
import uk.gov.justice.hmpps.prison.repository.jpa.model.ScheduledAppointment
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ScheduledAppointmentRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain
import uk.gov.justice.hmpps.prison.service.support.StringWithAbbreviationsProcessor
import uk.gov.justice.hmpps.prison.util.CalcDateRanges
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Collectors

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
class AppointmentsService(
  private val offenderBookingRepository: OffenderBookingRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val locationService: LocationService,
  private val referenceDomainService: ReferenceDomainService,
  private val telemetryClient: TelemetryClient,
  private val scheduledAppointmentRepository: ScheduledAppointmentRepository,
  private val offenderIndividualScheduleRepository: OffenderIndividualScheduleRepository,
  private val agencyLocationRepository: AgencyLocationRepository,
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository,
  private val eventStatusRepository: ReferenceCodeRepository<EventStatus>,
) {
  @Transactional
  fun createAppointments(@Valid appointments: AppointmentsToCreate): List<CreatedAppointmentDetails> {
    assertThatRequestHasPermission(appointments)
    assertFewerThanMaximumNumberOfBookingIds(appointments)

    val defaults = appointments.appointmentDefaults

    val appointmentLocation = agencyInternalLocationRepository.findByIdOrNull(defaults.locationId)
      ?: throw EntityNotFoundException.withId(defaults.locationId)

    val agencyId = findLocationInUserLocations(appointmentLocation)
      .orElseThrow(BadRequestException.withMessage("Location does not exist or is not in your caseload."))
      .agencyId

    assertValidAppointmentType(defaults.appointmentType) // GETS event type

    val flattenedDetails = appointments.withDefaults()

    assertAdditionalAppointmentConstraints(flattenedDetails)

    val appointmentsWithRepeats = withRepeats(appointments.repeat, flattenedDetails)
    assertThatAppointmentsFallWithin(appointmentsWithRepeats, appointmentTimeLimit())

    val eventStatus = eventStatusRepository.findByIdOrNull(EventStatus.SCHEDULED_APPROVED)
      ?: throw RuntimeException("Internal error: Event status not recognised.")
    val prison = agencyLocationRepository.findByIdOrNull(agencyId)
      ?: throw RuntimeException("Internal error: Prison not found")

    val createdAppointments =
      appointmentsWithRepeats.stream().map<CreatedAppointmentDetails> { a: AppointmentDetails ->
        val booking = offenderBookingRepository.findByIdOrNull(a.bookingId)
          ?.takeIf { booking -> agencyId == booking.location.id }
          ?: throw BadRequestException.withMessage("A BookingId does not exist in your caseload")

        val appointment = OffenderIndividualSchedule()
        appointment.offenderBooking = booking
        appointment.eventClass = EventClass.INT_MOV
        appointment.eventType = "APP"
        appointment.eventStatus = eventStatus
        appointment.eventSubType = defaults.appointmentType
        appointment.eventDate = a.startTime.toLocalDate()
        appointment.startTime = a.startTime
        appointment.endTime = a.endTime
        appointment.internalLocation = appointmentLocation
        appointment.fromLocation = prison
        appointment.comment = a.comment

        val savedAppointment = offenderIndividualScheduleRepository.save(appointment)
        CreatedAppointmentDetails.builder()
          .appointmentEventId(savedAppointment.id)
          .bookingId(a.bookingId)
          .startTime(a.startTime)
          .endTime(a.endTime)
          .appointmentType(defaults.appointmentType)
          .locationId(defaults.locationId)
          .build()
      }.collect(Collectors.toList<CreatedAppointmentDetails>())

    trackAppointmentsCreated(createdAppointments.size, defaults)

    return createdAppointments
  }

  @Transactional
  fun createBookingAppointment(
    bookingId: Long,
    username: String,
    @Valid appointmentSpecification: NewAppointment,
  ): ScheduledEvent {
    validateStartTime(appointmentSpecification)
    validateEndTime(appointmentSpecification)
    validateEventType(appointmentSpecification) // GETS event type

    val eventStatus = eventStatusRepository.findByIdOrNull(EventStatus.SCHEDULED_APPROVED)
      ?: throw RuntimeException("Internal error: Event status not recognised.")

    val validLocation = agencyInternalLocationRepository.findByIdOrNull(appointmentSpecification.locationId)
      ?.takeIf {
        authenticationFacade.isOverrideRole("GLOBAL_APPOINTMENT") ||
          locationService.getUserLocations(username, true).stream()
            .anyMatch { loc: Location -> loc.agencyId == it.agencyId }
      }
      ?: throw BadRequestException.withMessage("Location does not exist or is not in your caseload.")

    val booking = offenderBookingRepository.findByIdOrNull(bookingId)
      ?: throw RuntimeException("Internal error: Booking not found")

    val appointment = OffenderIndividualSchedule()
    appointment.offenderBooking = booking
    appointment.eventClass = EventClass.INT_MOV
    appointment.eventType = "APP"
    appointment.eventStatus = eventStatus
    appointment.eventSubType = appointmentSpecification.appointmentType
    appointment.eventDate = appointmentSpecification.startTime.toLocalDate()
    appointment.startTime = appointmentSpecification.startTime
    appointment.endTime = appointmentSpecification.endTime
    appointment.internalLocation = validLocation
    appointment.fromLocation = agencyLocationRepository.findByIdOrNull(validLocation.agencyId)
      ?: throw RuntimeException("Internal error: Prison not found")
    appointment.comment = appointmentSpecification.comment

    val savedAppointment = offenderIndividualScheduleRepository.saveAndFlush(appointment)

    val createdAppointment = getScheduledEventOrThrowEntityNotFound(savedAppointment.id)
    trackSingleAppointmentCreation(savedAppointment)
    return createdAppointment
  }

  @Transactional(readOnly = true)
  fun getBookingAppointment(appointmentId: Long): ScheduledEvent {
    return getScheduledEventOrThrowEntityNotFound(appointmentId)
  }

  @Transactional
  fun deleteBookingAppointments(appointmentIds: List<Long>) {
    appointmentIds.forEach { appointmentId: Long ->
      offenderIndividualScheduleRepository.findByIdOrNull(appointmentId)?.let { appointment ->
        offenderIndividualScheduleRepository.deleteById(appointmentId)
        trackAppointmentDeletion(appointment)
      }
    }
  }

  @Transactional
  fun deleteBookingAppointment(eventId: Long) {
    val appointmentForDeletion = offenderIndividualScheduleRepository.findByIdOrNull(eventId)
      ?: throw EntityNotFoundException.withMessage(
        "Booking Appointment for eventId %d not found.",
        eventId,
      )
    offenderIndividualScheduleRepository.deleteById(eventId)
    trackAppointmentDeletion(appointmentForDeletion)
  }

  @Transactional
  fun updateComment(appointmentId: Long, comment: String?) {
    val appointment = offenderIndividualScheduleRepository.findByIdOrNull(appointmentId)
      ?: throw EntityNotFoundException.withMessage(
        "An appointment with id %s does not exist.",
        appointmentId,
      )
    if (EventClass.INT_MOV != appointment.eventClass ||
      "APP" != appointment.eventType
    ) {
      throw EntityNotFoundException.withMessage("An appointment with id %s does not exist.", appointmentId)
    }
    appointment.comment = comment
  }

  private fun getScheduledEventOrThrowEntityNotFound(eventId: Long): ScheduledEvent = ScheduledEvent(
    offenderIndividualScheduleRepository.findByIdOrNull(eventId)
      ?: throw EntityNotFoundException.withMessage(
        "Booking Appointment for eventId %d not found.",
        eventId,
      ),
  )

  private fun validateStartTime(newAppointment: NewAppointment) {
    if (newAppointment.startTime.isBefore(LocalDateTime.now())) {
      throw BadRequestException.withMessage("Appointment time is in the past.")
    }
  }

  private fun validateEndTime(newAppointment: NewAppointment) {
    if (newAppointment.endTime != null &&
      newAppointment.endTime.isBefore(newAppointment.startTime)
    ) {
      throw BadRequestException.withMessage("Appointment end time is before the start time.")
    }
  }

  private fun validateEventType(newAppointment: NewAppointment) {
    val result = try {
      referenceDomainService.getReferenceCodeByDomainAndCode(
        ReferenceDomain.INTERNAL_SCHEDULE_REASON.domain,
        newAppointment.appointmentType,
        false,
      )
    } catch (ex: EntityNotFoundException) {
      Optional.empty()
    }

    if (result.isEmpty) {
      throw BadRequestException.withMessage("Event type not recognised.")
    }
  }

  private fun assertThatRequestHasPermission(appointments: AppointmentsToCreate) {
    if (appointments.moreThanOneOffender() && !AuthenticationFacade.hasRoles("BULK_APPOINTMENTS")) {
      throw BadRequestException.withMessage(
        "You do not have the 'BULK_APPOINTMENTS' role. Creating appointments for more than one offender is not permitted without this role.",
      )
    }
  }

  private fun assertThatAppointmentsFallWithin(appointments: List<AppointmentDetails>, limit: LocalDateTime) {
    for (appointment in appointments) {
      assertThatAppointmentFallsWithin(appointment, limit)
    }
  }

  private fun assertThatAppointmentFallsWithin(appointment: AppointmentDetails, limit: LocalDateTime) {
    if (appointment.startTime.isAfter(limit)) {
      throw BadRequestException.withMessage("An appointment startTime is later than the limit of $limit")
    }
    if (appointment.endTime == null) {
      return
    }
    if (appointment.endTime.isAfter(limit)) {
      throw BadRequestException.withMessage("An appointment endTime is later than the limit of $limit")
    }
  }

  private fun assertFewerThanMaximumNumberOfBookingIds(appointments: AppointmentsToCreate) {
    appointments.appointments
      ?.size
      ?.takeIf { it > MAXIMUM_NUMBER_OF_APPOINTMENTS }
      ?.run {
        throw BadRequestException.withMessage("Request to create $this appointments exceeds limit of $MAXIMUM_NUMBER_OF_APPOINTMENTS")
      }
  }

  private fun assertAdditionalAppointmentConstraints(appointments: List<AppointmentDetails>) {
    appointments.forEach { appointment: AppointmentDetails -> assertStartTimePrecedesEndTime(appointment) }
  }

  private fun assertValidAppointmentType(appointmentType: String) {
    findEventType(appointmentType).orElseThrow(
      BadRequestException.withMessage("Event type not recognised."),
    )
  }

  private fun findEventType(appointmentType: String): Optional<ReferenceCode> {
    return referenceDomainService.getReferenceCodeByDomainAndCode(
      ReferenceDomain.INTERNAL_SCHEDULE_REASON.domain,
      appointmentType,
      false,
    )
  }

  private fun findLocationInUserLocations(appointmentLocation: AgencyInternalLocation): Optional<Location> {
    val userLocations = locationService.getUserLocations(authenticationFacade.currentUsername, true)

    for (location in userLocations) {
      if (location.agencyId == appointmentLocation.agencyId) {
        return Optional.of(location)
      }
    }
    return Optional.empty()
  }

  fun getAppointments(
    agencyId: String,
    date: LocalDate,
    locationId: Long?,
    timeSlot: TimeSlot?,
  ): List<ScheduledAppointmentDto> {
    val appointmentStream =
      if (locationId != null) {
        scheduledAppointmentRepository.findByAgencyIdAndEventDateAndLocationId(
          agencyId,
          date,
          locationId,
        ).stream()
      } else {
        scheduledAppointmentRepository.findByAgencyIdAndEventDate(agencyId, date).stream()
      }

    val appointmentDtos = appointmentStream
      .map { scheduledAppointment: ScheduledAppointment ->
        ScheduledAppointmentDto
          .builder()
          .id(scheduledAppointment.eventId)
          .offenderNo(scheduledAppointment.offenderNo)
          .firstName(scheduledAppointment.firstName)
          .lastName(scheduledAppointment.lastName)
          .date(scheduledAppointment.eventDate)
          .startTime(scheduledAppointment.startTime)
          .endTime(scheduledAppointment.endTime)
          .appointmentTypeDescription(StringWithAbbreviationsProcessor.format(scheduledAppointment.appointmentTypeDescription))
          .appointmentTypeCode(scheduledAppointment.appointmentTypeCode)
          .locationDescription(StringWithAbbreviationsProcessor.format(scheduledAppointment.locationDescription))
          .locationId(scheduledAppointment.locationId)
          .createUserId(scheduledAppointment.createUserId)
          .agencyId(scheduledAppointment.agencyId)
          .build()
      }

    val filteredByTimeSlot = if (timeSlot != null) {
      appointmentDtos.filter { appointment: ScheduledAppointmentDto ->
        CalcDateRanges.eventStartsInTimeslot(
          appointment.startTime,
          timeSlot,
        )
      }
    } else {
      appointmentDtos
    }

    return filteredByTimeSlot
      .sorted(
        Comparator.comparing { obj: ScheduledAppointmentDto -> obj.startTime }
          .thenComparing { obj: ScheduledAppointmentDto -> obj.locationDescription },
      )
      .collect(Collectors.toList())
  }

  private fun trackAppointmentsCreated(appointmentsCreatedCount: Int?, defaults: AppointmentDefaults) {
    if (appointmentsCreatedCount == null || appointmentsCreatedCount < 1) return

    val logMap: MutableMap<String, String> = HashMap()
    logMap["type"] = defaults.appointmentType
    logMap["defaultStart"] = defaults.startTime.toString()
    logMap["location"] = defaults.locationId.toString()
    logMap["user"] = authenticationFacade.currentUsername
    if (defaults.endTime != null) {
      logMap["defaultEnd"] = defaults.endTime.toString()
    }
    logMap["count"] = appointmentsCreatedCount.toString()

    telemetryClient.trackEvent("AppointmentsCreated", logMap, null)
  }

  private fun trackSingleAppointmentCreation(appointment: OffenderIndividualSchedule) {
    telemetryClient.trackEvent("AppointmentCreated", appointmentEvent(appointment), null)
  }

  private fun trackAppointmentDeletion(appointment: OffenderIndividualSchedule) {
    telemetryClient.trackEvent("AppointmentDeleted", appointmentEvent(appointment), null)
  }

  private fun appointmentEvent(appointment: OffenderIndividualSchedule): Map<String, String> {
    val logMap: MutableMap<String, String> = HashMap()
    logMap["eventId"] = appointment.id.toString()
    logMap["user"] = authenticationFacade.currentUsername
    logMap["type"] = appointment.eventSubType
    logMap["agency"] = appointment.fromLocation.id

    if (appointment.startTime != null) {
      logMap["start"] = appointment.startTime.toString()
    }

    if (appointment.endTime != null) {
      logMap["end"] = appointment.endTime.toString()
    }
    if (appointment.internalLocation.locationId != null) {
      logMap["location"] = appointment.internalLocation.locationId.toString()
    }
    return logMap
  }

  companion object {
    // Maximum of 1000 values in an Oracle 'IN' clause is current hard limit. (See #validateBookingIds below).
    private const val MAXIMUM_NUMBER_OF_APPOINTMENTS = 1000
    private const val APPOINTMENT_TIME_LIMIT_IN_DAYS = 365

    private fun appointmentTimeLimit(): LocalDateTime {
      return LocalDateTime.now().plusDays(APPOINTMENT_TIME_LIMIT_IN_DAYS.toLong())
    }

    private fun assertStartTimePrecedesEndTime(appointment: AppointmentDetails) {
      if (appointment.endTime != null &&
        appointment.endTime.isBefore(appointment.startTime)
      ) {
        throw BadRequestException.withMessage("Appointment end time is before the start time.")
      }
    }

    fun withRepeats(repeat: Repeat?, details: List<AppointmentDetails>): List<AppointmentDetails> {
      return details.stream()
        .flatMap { d: AppointmentDetails -> withRepeats(repeat, d).stream() }
        .collect(Collectors.toList())
    }

    fun withRepeats(repeat: Repeat?, details: AppointmentDetails): List<AppointmentDetails> {
      if (repeat == null || repeat.count < 2) return listOf(details)

      val appointmentDuration = Optional
        .ofNullable(details.endTime)
        .map { endTime: LocalDateTime? -> Duration.between(details.startTime, endTime) }

      return repeat
        .dateTimeStream(details.startTime)
        .map { startTime: LocalDateTime ->
          buildFromPrototypeWithStartTimeAndDuration(
            details,
            startTime,
            appointmentDuration,
          )
        }
        .collect(Collectors.toList())
    }

    private fun buildFromPrototypeWithStartTimeAndDuration(
      prototype: AppointmentDetails,
      startTime: LocalDateTime,
      appointmentDuration: Optional<Duration>,
    ): AppointmentDetails {
      val builder = prototype.toBuilder().startTime(startTime)
      appointmentDuration.ifPresent { d: Duration -> builder.endTime(startTime.plus(d)) }
      return builder.build()
    }
  }
}
