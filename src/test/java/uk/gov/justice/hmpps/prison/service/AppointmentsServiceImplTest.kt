package uk.gov.justice.hmpps.prison.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.fail
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
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
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.RepeatPeriod
import uk.gov.justice.hmpps.prison.api.support.TimeSlot
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.ScheduledAppointment
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ScheduledAppointmentRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function

class AppointmentsServiceImplTest {
  @Mock
  private lateinit var bookingRepository: BookingRepository

  @Mock
  private lateinit var offenderBookingRepository: OffenderBookingRepository

  @Mock
  private lateinit var locationService: LocationService

  @Mock
  private lateinit var referenceDomainService: ReferenceDomainService

  @Mock
  private lateinit var scheduledAppointmentRepository: ScheduledAppointmentRepository

  @Mock
  private lateinit var offenderIndividualScheduleRepository: OffenderIndividualScheduleRepository

  @Mock
  private lateinit var agencyLocationRepository: AgencyLocationRepository

  @Mock
  private lateinit var agencyInternalLocationRepository: AgencyInternalLocationRepository

  @Mock
  private lateinit var eventStatusRepository: ReferenceCodeRepository<EventStatus>

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  private lateinit var appointmentsService: AppointmentsService

  private val prison = AgencyLocation().apply { description = "description" }

  @BeforeEach
  fun initMocks() {
    SecurityContextHolder.createEmptyContext()
    ensureRoles(BULK_APPOINTMENTS_ROLE)
    MockitoAnnotations.openMocks(this)

    whenever(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED)).thenReturn(
      Optional.of(EventStatus("SCH", "desc")),
    )
    whenever(agencyLocationRepository.findById(any())).thenAnswer { invocation ->
      Optional.of(prison.apply { id = invocation.getArgument<String>(0) })
    }
    whenever(offenderIndividualScheduleRepository.save(any())).thenAnswer { invocation ->
      invocation.getArgument<OffenderIndividualSchedule>(0).apply { id = NEW_EVENT_ID }
    }
    whenever(offenderIndividualScheduleRepository.saveAndFlush(any())).thenAnswer { invocation ->
      invocation.getArgument<OffenderIndividualSchedule>(0).apply { id = NEW_EVENT_ID }
    }

    appointmentsService = AppointmentsService(
      offenderBookingRepository,
      AuthenticationFacade(),
      locationService,
      referenceDomainService,
      telemetryClient,
      scheduledAppointmentRepository,
      offenderIndividualScheduleRepository,
      agencyLocationRepository,
      agencyInternalLocationRepository,
      eventStatusRepository,
    )
  }

  @AfterEach
  fun clearSecurityContext() {
    SecurityContextHolder.clearContext()
  }

  @Nested
  @DisplayName("Creating appointments")
  internal inner class CreateAppointments {
    @Test
    fun createAppointments_returnsMultipleAppointmentDetails() {
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)
      stubValidBookingIds(LOCATION_B.agencyId, DETAILS_1.bookingId, DETAILS_2.bookingId)

      val appointmentsToCreate = AppointmentsToCreate
        .builder()
        .appointmentDefaults(
          AppointmentDefaults
            .builder()
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .startTime(LocalDateTime.now().plusHours(1))
            .build(),
        )
        .appointments(listOf(DETAILS_1, DETAILS_2))
        .build()

      val appointment1 = appointmentsToCreate.withDefaults()[0]
      val createdId1 = 1L
      val appointment2 = appointmentsToCreate.withDefaults()[1]
      val createdId2 = 2L

      whenever(offenderIndividualScheduleRepository.save(ArgumentMatchers.any()))
        .thenReturn(appointmentWithId(createdId1))
        .thenReturn(appointmentWithId(createdId2))

      val createdAppointmentDetails = appointmentsService.createAppointments(appointmentsToCreate)

      assertThat(createdAppointmentDetails).hasSize(2)
        .containsExactlyInAnyOrder(
          CreatedAppointmentDetails.builder()
            .appointmentEventId(createdId1)
            .bookingId(appointment1.bookingId)
            .startTime(appointment1.startTime)
            .endTime(appointment1.endTime)
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .build(),
          CreatedAppointmentDetails.builder()
            .appointmentEventId(createdId2)
            .bookingId(appointment2.bookingId)
            .startTime(appointment2.startTime)
            .endTime(appointment2.endTime)
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .build(),
        )

      verify(telemetryClient).trackEvent(
        "AppointmentsCreated",
        mapOf(
          "defaultStart" to appointmentsToCreate.appointmentDefaults.startTime.toString(),
          "count" to "2",
          "type" to "T",
          "location" to "1",
          "user" to "username",
        ),
        null,
      )
    }

    @Test
    fun createAppointments_returnsRepeatAppointmentIds() {
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)
      stubValidBookingIds(LOCATION_B.agencyId, DETAILS_1.bookingId)

      val appointmentsToCreate = AppointmentsToCreate
        .builder()
        .appointmentDefaults(
          AppointmentDefaults
            .builder()
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .startTime(LocalDateTime.now().plusHours(1))
            .build(),
        )
        .appointments(listOf(DETAILS_1))
        .repeat(
          Repeat.builder()
            .count(3)
            .repeatPeriod(RepeatPeriod.DAILY)
            .build(),
        )
        .build()

      val createdId1 = 1L
      val recurringId1 = 2L
      val recurringId2 = 3L

      val appointmentWithRepeats = AppointmentsService
        .withRepeats(appointmentsToCreate.repeat, appointmentsToCreate.withDefaults()[0])

      whenever(offenderIndividualScheduleRepository.save(ArgumentMatchers.any()))
        .thenReturn(appointmentWithId(createdId1))
        .thenReturn(appointmentWithId(recurringId1))
        .thenReturn(appointmentWithId(recurringId2))

      val createdAppointmentDetails = appointmentsService.createAppointments(appointmentsToCreate)

      assertThat(createdAppointmentDetails).hasSize(3)
        .containsExactlyInAnyOrder(
          CreatedAppointmentDetails.builder()
            .appointmentEventId(createdId1)
            .bookingId(appointmentWithRepeats[0].bookingId)
            .startTime(appointmentWithRepeats[0].startTime)
            .endTime(appointmentWithRepeats[0].endTime)
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .build(),
          CreatedAppointmentDetails.builder()
            .appointmentEventId(recurringId1)
            .bookingId(appointmentWithRepeats[1].bookingId)
            .startTime(appointmentWithRepeats[1].startTime)
            .endTime(appointmentWithRepeats[1].endTime)
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .build(),
          CreatedAppointmentDetails.builder()
            .appointmentEventId(recurringId2)
            .bookingId(appointmentWithRepeats[2].bookingId)
            .startTime(appointmentWithRepeats[2].startTime)
            .endTime(appointmentWithRepeats[2].endTime)
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .build(),
        )

      verify(telemetryClient).trackEvent(
        "AppointmentsCreated",
        mapOf(
          "defaultStart" to appointmentsToCreate.appointmentDefaults.startTime.toString(),
          "count" to "3",
          "type" to "T",
          "location" to "1",
          "user" to "username",
        ),
        null,
      )
    }

    @Test
    fun createTooManyAppointments() {
      assertThatThrownBy {
        appointmentsService.createAppointments(
          AppointmentsToCreate
            .builder()
            .appointmentDefaults(
              AppointmentDefaults
                .builder()
                .build(),
            )
            .appointments(arrayOfNulls<AppointmentDetails>(1001).toList())
            .build(),
        )
      }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("Request to create 1001 appointments exceeds limit of 1000")

      verifyNoMoreInteractions(telemetryClient)
    }

    @Test
    fun locationNotInCaseload() {
      stubLocation(LOCATION_C)

      assertThatThrownBy {
        appointmentsService.createAppointments(
          AppointmentsToCreate
            .builder()
            .appointmentDefaults(
              AppointmentDefaults
                .builder()
                .locationId(LOCATION_C.locationId)
                .build(),
            )
            .appointments(listOf())
            .build(),
        )
      }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("Location does not exist or is not in your caseload.")

      verifyNoMoreInteractions(telemetryClient)
    }

    @Test
    fun unknownAppointmentType() {
      stubLocation(LOCATION_B)

      assertThatThrownBy {
        appointmentsService.createAppointments(
          AppointmentsToCreate
            .builder()
            .appointmentDefaults(
              AppointmentDefaults
                .builder()
                .locationId(LOCATION_B.locationId)
                .appointmentType("NOT_KNOWN")
                .startTime(LocalDateTime.now())
                .build(),
            )
            .appointments(listOf())
            .build(),
        )
      }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("Event type not recognised.")

      verifyNoMoreInteractions(telemetryClient)
    }

    @Test
    fun createNoAppointments() {
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)

      appointmentsService.createAppointments(
        AppointmentsToCreate
          .builder()
          .appointmentDefaults(
            AppointmentDefaults
              .builder()
              .locationId(LOCATION_B.locationId)
              .appointmentType(REFERENCE_CODE_T.code)
              .startTime(LocalDateTime.now())
              .build(),
          )
          .appointments(listOf())
          .build(),
      )

      verifyNoMoreInteractions(telemetryClient)
    }

    @Test
    fun bookingIdNotInCaseload() {
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)

      assertThatThrownBy {
        appointmentsService.createAppointments(
          AppointmentsToCreate
            .builder()
            .appointmentDefaults(
              AppointmentDefaults
                .builder()
                .locationId(LOCATION_B.locationId)
                .appointmentType(REFERENCE_CODE_T.code)
                .startTime(LocalDateTime.now().plusHours(1))
                .build(),
            )
            .appointments(listOf(DETAILS_1))
            .build(),
        )
      }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("A BookingId does not exist in your caseload")

      verifyNoMoreInteractions(telemetryClient)
    }

    @Test
    fun bookingIdInCaseload() {
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)
      stubValidBookingIds(LOCATION_B.agencyId, DETAILS_1.bookingId)

      val appointmentsToCreate = AppointmentsToCreate
        .builder()
        .appointmentDefaults(
          AppointmentDefaults
            .builder()
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .startTime(LocalDateTime.now().plusHours(1))
            .build(),
        )
        .appointments(listOf(DETAILS_1))
        .build()

      appointmentsService.createAppointments(appointmentsToCreate)

      verify(offenderIndividualScheduleRepository)
        .save(
          check {
            assertThat(it.eventSubType).isEqualTo(appointmentsToCreate.appointmentDefaults.appointmentType)
            assertThat(it.fromLocation).isEqualTo(prison)
          },
        )
      verify(telemetryClient).trackEvent(
        ArgumentMatchers.eq("AppointmentsCreated"),
        ArgumentMatchers.anyMap(),
        ArgumentMatchers.isNull(),
      )
    }

    @Test
    fun appointmentStartTimeTooLate() {
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)
      stubValidBookingIds(LOCATION_B.agencyId, DETAILS_1.bookingId)

      val appointmentsToCreate = AppointmentsToCreate
        .builder()
        .appointmentDefaults(
          AppointmentDefaults
            .builder()
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .startTime(LocalDateTime.now().plusHours(1L))
            .build(),
        )
        .appointments(listOf(DETAILS_1))
        .repeat(
          Repeat
            .builder()
            .count(13)
            .repeatPeriod(RepeatPeriod.MONTHLY)
            .build(),
        )
        .build()

      assertThatThrownBy { appointmentsService.createAppointments(appointmentsToCreate) }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("An appointment startTime is later than the limit of ")
    }

    @Test
    fun appointmentEndTimeTooLate() {
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)
      stubValidBookingIds(LOCATION_B.agencyId, DETAILS_1.bookingId)

      val appointmentsToCreate = AppointmentsToCreate
        .builder()
        .appointmentDefaults(
          AppointmentDefaults
            .builder()
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .startTime(LocalDateTime.now().plusHours(1L))
            .endTime(LocalDateTime.now().plusDays(31L).plusHours(1L))
            .build(),
        )
        .appointments(listOf(DETAILS_1))
        .repeat(
          Repeat
            .builder()
            .count(12)
            .repeatPeriod(RepeatPeriod.MONTHLY)
            .build(),
        )
        .build()

      assertThatThrownBy { appointmentsService.createAppointments(appointmentsToCreate) }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("An appointment endTime is later than the limit of ")
    }

    @Test
    fun rejectEndTimeBeforeStartTime() {
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)
      stubValidBookingIds(LOCATION_B.agencyId, DETAILS_1.bookingId)

      val appointmentsToCreate = AppointmentsToCreate
        .builder()
        .appointmentDefaults(
          AppointmentDefaults
            .builder()
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .startTime(LocalDateTime.now().plusHours(2L))
            .endTime(LocalDateTime.now().plusHours(1L))
            .build(),
        )
        .appointments(listOf(DETAILS_1))
        .build()

      assertThatThrownBy { appointmentsService.createAppointments(appointmentsToCreate) }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("Appointment end time is before the start time.")
    }

    /**
     * Also shows that BULK_APPOINTMENTS role is not required when creating appointments for a single offender.
     */
    @Test
    fun acceptEndTimeSameAsStartTime() {
      ensureRoles() // No roles
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)
      stubValidBookingIds(LOCATION_B.agencyId, DETAILS_1.bookingId)

      val in1Hour = LocalDateTime.now().plusHours(1)

      val appointmentsToCreate = AppointmentsToCreate
        .builder()
        .appointmentDefaults(
          AppointmentDefaults
            .builder()
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .startTime(in1Hour)
            .endTime(in1Hour)
            .build(),
        )
        .appointments(listOf(DETAILS_1))
        .build()

      appointmentsService.createAppointments(appointmentsToCreate)

      verify(offenderIndividualScheduleRepository).save(
        check {
          assertThat(it.eventSubType).isEqualTo(appointmentsToCreate.appointmentDefaults.appointmentType)
          assertThat(it.fromLocation).isEqualTo(prison)
        },
      )
    }

    @Test
    fun rejectMultipleOffendersWithoutBulkAppointmentRole() {
      ensureRoles() // No roles
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)
      stubValidBookingIds(LOCATION_B.agencyId, DETAILS_1.bookingId, DETAILS_2.bookingId)

      val appointmentsToCreate = AppointmentsToCreate
        .builder()
        .appointmentDefaults(
          AppointmentDefaults
            .builder()
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .startTime(LocalDateTime.now().plusHours(1))
            .build(),
        )
        .appointments(listOf(DETAILS_1, DETAILS_2))
        .build()

      assertThatThrownBy { appointmentsService.createAppointments(appointmentsToCreate) }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("You do not have the 'BULK_APPOINTMENTS' role. Creating appointments for more than one offender is not permitted without this role.")
    }

    @Test
    fun permitMultipleOffendersWithBulkAppointmentRole() {
      stubLocation(LOCATION_B)
      stubValidReferenceCode(REFERENCE_CODE_T)
      stubValidBookingIds(LOCATION_B.agencyId, DETAILS_1.bookingId, DETAILS_2.bookingId)

      val appointmentsToCreate = AppointmentsToCreate
        .builder()
        .appointmentDefaults(
          AppointmentDefaults
            .builder()
            .locationId(LOCATION_B.locationId)
            .appointmentType(REFERENCE_CODE_T.code)
            .startTime(LocalDateTime.now().plusHours(1))
            .build(),
        )
        .appointments(listOf(DETAILS_1, DETAILS_2))
        .build()

      appointmentsService.createAppointments(appointmentsToCreate)

      verify(offenderIndividualScheduleRepository, times(2)).save(
        check {
          assertThat(it.eventSubType).isEqualTo(appointmentsToCreate.appointmentDefaults.appointmentType)
          assertThat(it.fromLocation).isEqualTo(prison)
        },
      )
    }
  }

  @Nested
  @DisplayName("Repeatable appointments")
  internal inner class Repeats {
    @Test
    fun shouldHandleNoRepeats() {
      assertThat(AppointmentsService.withRepeats(null, listOf(DETAILS_2)))
        .containsExactly(DETAILS_2)
    }

    @Test
    fun shouldHandleOneRepeat() {
      assertThat(
        AppointmentsService.withRepeats(
          Repeat.builder().repeatPeriod(RepeatPeriod.DAILY).count(1).build(),
          listOf(DETAILS_2),
        ),
      )
        .containsExactly(DETAILS_2)
    }

    @Test
    fun shouldHandleMultipleRepeats() {
      assertThat(
        AppointmentsService.withRepeats(
          Repeat.builder().repeatPeriod(RepeatPeriod.DAILY).count(3).build(),
          listOf(DETAILS_2),
        ),
      )
        .containsExactly(
          DETAILS_2,
          DETAILS_2.toBuilder().startTime(DETAILS_2.startTime.plusDays(1))
            .endTime(DETAILS_2.endTime.plusDays(1)).build(),
          DETAILS_2.toBuilder().startTime(DETAILS_2.startTime.plusDays(2))
            .endTime(DETAILS_2.endTime.plusDays(2)).build(),
        )
    }

    @Test
    fun shouldRepeatMultipleAppointments() {
      assertThat(
        AppointmentsService.withRepeats(
          Repeat.builder().repeatPeriod(RepeatPeriod.DAILY).count(3).build(),
          listOf(DETAILS_2, DETAILS_3),
        ),
      )
        .containsExactly(
          DETAILS_2,
          DETAILS_2.toBuilder().startTime(DETAILS_2.startTime.plusDays(1))
            .endTime(DETAILS_2.endTime.plusDays(1)).build(),
          DETAILS_2.toBuilder().startTime(DETAILS_2.startTime.plusDays(2))
            .endTime(DETAILS_2.endTime.plusDays(2)).build(),
          DETAILS_3,
          DETAILS_3.toBuilder().startTime(DETAILS_3.startTime.plusDays(1)).build(),
          DETAILS_3.toBuilder().startTime(DETAILS_3.startTime.plusDays(2)).build(),
        )
    }

    @Test
    fun shouldHandleNullEndTime() {
      assertThat(
        AppointmentsService.withRepeats(
          Repeat.builder().repeatPeriod(RepeatPeriod.DAILY).count(2).build(),
          listOf(DETAILS_3),
        ),
      )
        .containsExactly(
          DETAILS_3,
          DETAILS_3.toBuilder().startTime(DETAILS_3.startTime.plusDays(1)).build(),
        )
    }
  }

  @Nested
  @DisplayName("Creating a single appointment for a booking")
  internal inner class CreatedBookingAppointments {
    @Test
    fun testCreateBookingAppointment() {
      val appointmentType = "MEDE"
      val locationId = -20L
      val bookingId = 100L
      val agencyId = "LEI"
      val principal = "ME"
      val createdEventId = 999L
      val startTime = LocalDateTime.now().plusDays(1)
      val endTime = LocalDateTime.now().plusDays(2)

      val expectedEvent = ScheduledEvent
        .builder()
        .bookingId(bookingId)
        .eventId(createdEventId)
        .eventLocationId(locationId)
        .eventLocation("description")
        .eventSource("APP")
        .eventSourceCode("APP")
        .eventSourceDesc("comment")
        .eventClass("INT_MOV")
        .eventStatus("SCH")
        .eventType("APP")
        .eventTypeDesc("Appointment")
        .eventSubType("MEDE")
        .eventDate(LocalDate.now().plusDays(1))
        .startTime(startTime)
        .endTime(endTime)
        .agencyId("LEI")
        .build()
      val location = Location.builder().locationId(locationId).agencyId(agencyId).build()

      val newAppointment = NewAppointment.builder()
        .appointmentType(appointmentType)
        .startTime(startTime)
        .endTime(endTime)
        .comment("comment")
        .locationId(locationId).build()

      whenever(agencyInternalLocationRepository.findById(location.locationId)).thenReturn(
        Optional.of(
          AgencyInternalLocation().apply {
            this.locationId = location.locationId
            this.agencyId = location.agencyId
          },
        ),
      )
      whenever(locationService.getUserLocations(principal, true)).thenReturn(listOf(location))

      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(
          ReferenceDomain.INTERNAL_SCHEDULE_REASON.domain,
          newAppointment.appointmentType,
          false,
        ),
      )
        .thenReturn(Optional.of(ReferenceCode.builder().code(appointmentType).build()))
      whenever(offenderBookingRepository.findById(bookingId)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            setBookingId(bookingId)
          },
        ),
      )
      whenever(agencyInternalLocationRepository.findById(locationId)).thenReturn(
        Optional.of(AgencyInternalLocation().apply { setLocationId(locationId) }),
      )

      val savedAppointment: AtomicReference<OffenderIndividualSchedule> = AtomicReference()
      whenever(offenderIndividualScheduleRepository.saveAndFlush(any())).thenAnswer { invocation ->
        invocation.getArgument<OffenderIndividualSchedule>(0).apply {
          id = createdEventId
          savedAppointment.set(this)
        }
      }
      whenever(offenderIndividualScheduleRepository.findById(createdEventId)).thenAnswer {
        return@thenAnswer Optional.of(savedAppointment.get())
      }

      val actualEvent = appointmentsService.createBookingAppointment(bookingId, principal, newAppointment)

      assertThat(actualEvent).isEqualTo(expectedEvent)
    }

    @Test
    fun testCreateBookingAppointmentInvalidStartTime() {
      val bookingId = 100L
      val principal = "ME"

      val newAppointment = NewAppointment.builder().startTime(LocalDateTime.now().plusDays(-1))
        .endTime(LocalDateTime.now().plusDays(2)).build()

      try {
        appointmentsService.createBookingAppointment(bookingId, principal, newAppointment)
        fail("Should have thrown exception")
      } catch (e: BadRequestException) {
        assertThat(e.message).isEqualTo("Appointment time is in the past.")
      }
    }

    @Test
    fun testCreateBookingAppointmentInvalidEndTime() {
      val bookingId = 100L
      val principal = "ME"

      val newAppointment = NewAppointment.builder().startTime(LocalDateTime.now().plusDays(2))
        .endTime(LocalDateTime.now().plusDays(1)).build()

      try {
        appointmentsService.createBookingAppointment(bookingId, principal, newAppointment)
        fail("Should have thrown exception")
      } catch (e: BadRequestException) {
        assertThat(e.message).isEqualTo("Appointment end time is before the start time.")
      }
    }

    @Test
    fun testCreateBookingAppointmentInvalidLocation() {
      val appointmentType = "MEDE"
      val locationId = -20L
      val bookingId = 100L
      val agencyId = "LEI"
      val eventId = -10L
      val principal = "ME"
      val expectedEvent = ScheduledEvent.builder().bookingId(bookingId).build()
      val location = Location.builder().locationId(locationId).agencyId(agencyId).build()

      val newAppointment = NewAppointment.builder()
        .appointmentType(appointmentType)
        .startTime(LocalDateTime.now().plusDays(1))
        .endTime(LocalDateTime.now().plusDays(2))
        .comment("comment")
        .locationId(locationId).build()

      whenever(locationService.getLocation(newAppointment.locationId)).thenReturn(location)
      whenever(locationService.getUserLocations(principal, true)).thenReturn(listOf(location))

      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(ReferenceDomain.INTERNAL_SCHEDULE_REASON.domain, newAppointment.appointmentType, false),
      )
        .thenReturn(Optional.of(ReferenceCode.builder().code(appointmentType).build()))

      whenever(bookingRepository.getBookingAppointmentByEventId(eventId)).thenReturn(Optional.of(expectedEvent))

      whenever(locationService.getLocation(newAppointment.locationId)).thenThrow(EntityNotFoundException("test"))

      assertThatThrownBy {
        appointmentsService.createBookingAppointment(
          bookingId,
          principal,
          newAppointment,
        )
      }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("Location does not exist or is not in your caseload.")
    }

    @Test
    fun testCreateBookingAppointmentInvalidAppointmentType() {
      val appointmentType = "MEDE"
      val locationId = -20L
      val bookingId = 100L
      val agencyId = "LEI"
      val eventId = -10L
      val principal = "ME"
      val expectedEvent = ScheduledEvent.builder().bookingId(bookingId).build()
      val location = Location.builder().locationId(locationId).agencyId(agencyId).build()

      val newAppointment = NewAppointment.builder().appointmentType(appointmentType)
        .startTime(LocalDateTime.now().plusDays(1)).endTime(LocalDateTime.now().plusDays(2)).comment("comment")
        .locationId(locationId).build()

      whenever(locationService.getLocation(newAppointment.locationId)).thenReturn(location)
      whenever(locationService.getUserLocations(principal, true)).thenReturn(listOf(location))

      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(ReferenceDomain.INTERNAL_SCHEDULE_REASON.domain, newAppointment.appointmentType, false),
      )
        .thenReturn(Optional.of(ReferenceCode.builder().code(appointmentType).build()))

      whenever(bookingRepository.getBookingAppointmentByEventId(eventId)).thenReturn(Optional.of(expectedEvent))

      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(ReferenceDomain.INTERNAL_SCHEDULE_REASON.domain, newAppointment.appointmentType, false),
      )
        .thenReturn(Optional.empty())

      assertThatThrownBy {
        appointmentsService.createBookingAppointment(
          bookingId,
          principal,
          newAppointment,
        )
      }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("Event type not recognised.")
    }

    @Test
    fun testOverrideAgencyLocationTest() {
      val appointmentType = "MEDE"
      val locationId = -20L
      val bookingId = 100L
      val agencyId = "LEI"
      val principal = "ME"

      val location = Location.builder().locationId(locationId).agencyId(agencyId).build()

      val newAppointment = NewAppointment.builder()
        .appointmentType(appointmentType)
        .startTime(LocalDateTime.now().plusDays(1))
        .endTime(LocalDateTime.now().plusDays(2))
        .comment("comment")
        .locationId(locationId).build()

      ensureRoles("GLOBAL_APPOINTMENT")

      whenever(agencyInternalLocationRepository.findById(location.locationId)).thenReturn(
        Optional.of(
          AgencyInternalLocation().apply {
            this.locationId = location.locationId
            this.agencyId = location.agencyId
          },
        ),
      )
      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(ReferenceDomain.INTERNAL_SCHEDULE_REASON.domain, newAppointment.appointmentType, false),
      )
        .thenReturn(Optional.of(ReferenceCode.builder().code(appointmentType).build()))

      whenever(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(OffenderBooking()))
      whenever(agencyInternalLocationRepository.findById(location.locationId)).thenReturn(
        Optional.of(AgencyInternalLocation()),
      )

      val savedAppointment: AtomicReference<OffenderIndividualSchedule> = AtomicReference()
      whenever(offenderIndividualScheduleRepository.saveAndFlush(any())).thenAnswer { invocation ->
        invocation.getArgument<OffenderIndividualSchedule>(0).apply {
          id = NEW_EVENT_ID
          savedAppointment.set(this)
        }
      }
      whenever(offenderIndividualScheduleRepository.findById(NEW_EVENT_ID)).thenAnswer {
        return@thenAnswer Optional.of(savedAppointment.get())
      }

      appointmentsService.createBookingAppointment(bookingId, principal, newAppointment)

      verify(locationService, never()).getUserLocations(principal, true)
    }
  }

  @Nested
  @DisplayName("Get appointments")
  internal inner class GetAppointment {
    @Test
    fun testFindByAgencyIdAndEventDateAndLocationId_IsCalledCorrectly() {
      val today = LocalDate.now()
      val locationId = 1L

      whenever(
        scheduledAppointmentRepository.findByAgencyIdAndEventDateAndLocationId(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.anyLong(),
        ),
      ).thenReturn(
        emptyList(),
      )

      appointmentsService.getAppointments("LEI", today, locationId, null)

      verify(scheduledAppointmentRepository)
        .findByAgencyIdAndEventDateAndLocationId("LEI", today, locationId)
    }

    @Test
    fun testFindByAgencyIdAndEventDate_IsCalledCorrectly_IsCalledCorrectly() {
      val today = LocalDate.now()

      whenever(
        scheduledAppointmentRepository.findByAgencyIdAndEventDate(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
        ),
      )
        .thenReturn(emptyList())

      appointmentsService.getAppointments("LEI", today, null, null)

      verify(scheduledAppointmentRepository).findByAgencyIdAndEventDate("LEI", today)
    }

    @Test
    fun testAMScheduledAppointmentDtos_AreReturned() {
      val today = LocalDate.now()
      val startTime = LocalDateTime.now()
      val endTime = LocalDateTime.now()

      whenever(
        scheduledAppointmentRepository.findByAgencyIdAndEventDate(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
        ),
      )
        .thenReturn(
          listOf(
            ScheduledAppointment
              .builder()
              .eventId(1L)
              .offenderNo("A12345")
              .firstName("firstName1")
              .lastName("lastName1")
              .eventDate(today)
              .startTime(startTime.withHour(11))
              .endTime(endTime.withHour(11))
              .appointmentTypeDescription("Appointment Type Description1")
              .appointmentTypeCode("appointmentTypeCode1")
              .locationDescription("location Description1")
              .locationId(1L)
              .createUserId("Staff user 1")
              .agencyId("LEI")
              .build(),
            ScheduledAppointment
              .builder()
              .eventId(2L)
              .offenderNo("A12346")
              .firstName("firstName2")
              .lastName("lastName2")
              .eventDate(today)
              .startTime(startTime.withHour(23))
              .endTime(endTime.withHour(23))
              .appointmentTypeDescription("appointmentTypeDescription2")
              .appointmentTypeCode("appointmentTypeCode2")
              .locationDescription("location Description2")
              .locationId(2L)
              .createUserId("Staff user 2")
              .agencyId("LEI")
              .build(),
          ),
        )

      val appointmentDtos = appointmentsService.getAppointments("LEI", today, null, TimeSlot.AM)

      assertThat(appointmentDtos).containsOnly(
        ScheduledAppointmentDto
          .builder()
          .id(1L)
          .offenderNo("A12345")
          .firstName("firstName1")
          .lastName("lastName1")
          .date(today)
          .startTime(startTime.withHour(11))
          .endTime(endTime.withHour(11))
          .appointmentTypeDescription("Appointment Type Description1")
          .appointmentTypeCode("appointmentTypeCode1")
          .locationDescription("Location Description1")
          .locationId(1L)
          .createUserId("Staff user 1")
          .agencyId("LEI")
          .build(),
      )
    }

    @Test
    fun testScheduledAppointmentsOrderedByStartTimeThenByLocation() {
      val baseDateTime = LocalDateTime.now().minusDays(1)

      whenever(
        scheduledAppointmentRepository.findByAgencyIdAndEventDate(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
        ),
      )
        .thenReturn(
          listOf(
            ScheduledAppointment.builder().eventId(1L).startTime(baseDateTime.withHour(23))
              .locationDescription("Gym").build(),
            ScheduledAppointment.builder().eventId(2L).startTime(baseDateTime.withHour(11))
              .locationDescription("Room 2").build(),
            ScheduledAppointment.builder().eventId(3L).startTime(baseDateTime.withHour(10))
              .locationDescription("Z").build(),
            ScheduledAppointment.builder().eventId(4L).startTime(baseDateTime.withHour(10))
              .locationDescription("A").build(),
          ),
        )

      val appointmentDtos = appointmentsService.getAppointments("LEI", LocalDate.now(), null, null)

      assertThat(appointmentDtos)
        .extracting(
          Function<ScheduledAppointmentDto, Any> { obj: ScheduledAppointmentDto -> obj.id },
          Function<ScheduledAppointmentDto, Any> { obj: ScheduledAppointmentDto -> obj.locationDescription },
        ).containsExactly(
          Tuple.tuple(4L, "A"),
          Tuple.tuple(3L, "Z"),
          Tuple.tuple(2L, "Room 2"),
          Tuple.tuple(1L, "Gym"),
        )
    }
  }

  @Nested
  @DisplayName("Delete a single appointment")
  internal inner class DeleteSingleAppointment {
    @Test
    fun deleteBookingAppointment_notFound() {
      whenever(offenderIndividualScheduleRepository.findById(1L)).thenReturn(Optional.empty())

      assertThatThrownBy { appointmentsService.deleteBookingAppointment(1L) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Booking Appointment for eventId 1 not found.")
    }

    @Test
    fun deleteBookingAppointment() {
      whenever(offenderIndividualScheduleRepository.findById(1L)).thenReturn(
        Optional.of(
          OffenderIndividualSchedule().apply {
            id = 1L
            eventType = "APP"
            eventSubType = "VLB"
            startTime = LocalDateTime.of(2020, 1, 1, 1, 1)
            endTime = LocalDateTime.of(2020, 1, 1, 1, 31)
            internalLocation = AgencyInternalLocation().apply { locationId = 2L }
            fromLocation = AgencyLocation().apply { id = "WWI" }
          },
        ),
      )

      appointmentsService.deleteBookingAppointment(1L)

      verify(offenderIndividualScheduleRepository).deleteById(1L)

      verify(telemetryClient).trackEvent(
        "AppointmentDeleted",
        mapOf(
          "eventId" to "1",
          "type" to "VLB",
          "start" to "2020-01-01T01:01",
          "end" to "2020-01-01T01:31",
          "location" to "2",
          "agency" to "WWI",
          "user" to "username",
        ),
        null,
      )
    }
  }

  @Nested
  @DisplayName("Delete multiple appointments")
  internal inner class DeleteMultipleAppointments {
    @Test
    fun attemptToDeleteAppointmentsThatExist() {
      whenever(offenderIndividualScheduleRepository.findById(1L)).thenReturn(
        Optional.of(
          OffenderIndividualSchedule().apply {
            id = 1L
            eventSubType = "APP"
            startTime = LocalDateTime.of(2020, 1, 1, 1, 1)
            endTime = LocalDateTime.of(2020, 1, 1, 1, 31)
            internalLocation = AgencyInternalLocation().apply { locationId = 2L }
            fromLocation = AgencyLocation().apply { id = "LEI" }
          },
        ),
      )
      whenever(offenderIndividualScheduleRepository.findById(2L)).thenReturn(Optional.empty())

      appointmentsService.deleteBookingAppointments(listOf(1L, 2L))

      verify(offenderIndividualScheduleRepository, times(1)).deleteById(1L)
      verify(offenderIndividualScheduleRepository, never()).deleteById(2L)

      verify(telemetryClient).trackEvent(
        "AppointmentDeleted",
        mapOf(
          "eventId" to "1",
          "type" to "APP",
          "start" to "2020-01-01T01:01",
          "end" to "2020-01-01T01:31",
          "location" to "2",
          "agency" to "LEI",
          "user" to "username",
        ),
        null,
      )
    }
  }

  private fun stubValidBookingIds(agencyId: String, vararg bookingIds: Long) {
    val ids = bookingIds.toList()
    ids.forEach {
      whenever(offenderBookingRepository.findById(it)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            location = AgencyLocation().apply { id = agencyId }
          },
        ),
      )
    }
  }

  private fun stubValidReferenceCode(code: ReferenceCode) {
    whenever(
      referenceDomainService.getReferenceCodeByDomainAndCode(
        ReferenceDomain.INTERNAL_SCHEDULE_REASON.domain,
        code.code,
        false,
      ),
    )
      .thenReturn(Optional.of(REFERENCE_CODE_T))
  }

  private fun stubLocation(location: Location) {
    whenever(locationService.getUserLocations(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean()))
      .thenReturn(
        listOf(LOCATION_A, LOCATION_B),
      )
    whenever(locationService.getLocation(location.locationId)).thenReturn(location)
    whenever(agencyInternalLocationRepository.findById(location.locationId)).thenReturn(
      Optional.of(
        AgencyInternalLocation().apply {
          locationId = location.locationId
          agencyId = location.agencyId
        },
      ),
    )
  }

  private fun ensureRoles(vararg roles: String) {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken(USERNAME, null, *roles)
  }

  companion object {
    private const val USERNAME = "username"
    private const val BULK_APPOINTMENTS_ROLE = "BULK_APPOINTMENTS"

    private const val NEW_EVENT_ID = 123456L

    private val LOCATION_A: Location = Location.builder().locationId(0L).agencyId("A").build()
    private val LOCATION_B: Location = Location.builder().locationId(1L).agencyId("B").build()
    private val LOCATION_C: Location = Location.builder().locationId(2L).agencyId("C").build()

    private val DETAILS_1: AppointmentDetails = AppointmentDetails.builder().bookingId(1L).build()

    private val DETAILS_2: AppointmentDetails = AppointmentDetails
      .builder()
      .bookingId(2L)
      .startTime(LocalDateTime.of(2018, 2, 27, 13, 30)) // Tuesday
      .endTime(LocalDateTime.of(2018, 2, 27, 13, 50))
      .build()

    private val DETAILS_3: AppointmentDetails = AppointmentDetails
      .builder()
      .bookingId(2L)
      .startTime(LocalDateTime.of(2018, 2, 27, 13, 30)) // Tuesday
      .build()

    private val REFERENCE_CODE_T: ReferenceCode = ReferenceCode
      .builder()
      .activeFlag("Y")
      .code("T")
      .domain(ReferenceDomain.INTERNAL_SCHEDULE_REASON.domain)
      .build()

    private fun appointmentWithId(createdId1: Long): OffenderIndividualSchedule {
      val a1 = OffenderIndividualSchedule()
      a1.id = createdId1
      return a1
    }
  }
}
