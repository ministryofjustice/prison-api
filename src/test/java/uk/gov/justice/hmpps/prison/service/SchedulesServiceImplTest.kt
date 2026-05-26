@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.api.model.PrisonerPrisonSchedule
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.api.support.TimeSlot
import uk.gov.justice.hmpps.prison.repository.ScheduleRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerActivitiesCount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ScheduledActivityRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month

internal class SchedulesServiceImplTest {
  private val locationService: LocationService = mock()
  private val inmateService: InmateService = mock()
  private val bookingService: BookingService = mock()
  private val referenceDomainService: ReferenceDomainService = mock()
  private val scheduleRepository: ScheduleRepository = mock()
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder = mock()
  private val scheduledActivityRepository: ScheduledActivityRepository = mock()

  private val schedulesService = SchedulesService(
    locationService,
    inmateService,
    bookingService,
    referenceDomainService,
    scheduleRepository,
    hmppsAuthenticationHolder,
    scheduledActivityRepository,
    MAX_BATCH_SIZE,
  )

  @Test
  fun testGetLocationEventsAppAM() {
    val app = PrisonerSchedule.builder()
      .cellLocation("M0")
      .offenderNo("A10")
      .startTime(TIME_1000)
      .event("APP")
      .build()
    val apps = listOf(app)
    whenever(
      scheduleRepository.getLocationAppointments(
        -100L,
        DATE,
        DATE,
        "lastName",
        Order.ASC,
      ),
    ).thenReturn(apps)

    val results = schedulesService.getLocationEvents(-100L, "APP", DATE, TimeSlot.AM, null, null)
    assertThat(results[0].offenderNo).isEqualTo("A10")
  }

  @Test
  fun testGetLocationEventsVisitPM() {
    val visit = PrisonerSchedule.builder()
      .cellLocation("M0")
      .offenderNo("A10")
      .startTime(LocalDateTime.of(DATE, LocalTime.of(14, 0)))
      .event("VISIT")
      .build()
    val visits = listOf(visit)
    whenever(
      scheduleRepository.getLocationVisits(
        -100L,
        DATE,
        DATE,
        "lastName",
        Order.ASC,
      ),
    ).thenReturn(visits)

    val results = schedulesService.getLocationEvents(-100L, "VISIT", DATE, TimeSlot.PM, null, null)
    assertThat(results[0].offenderNo).isEqualTo("A10")
  }

  @Test
  fun testGetLocationEventsActivityED() {
    val visit = PrisonerSchedule.builder()
      .cellLocation("M0")
      .offenderNo("A10")
      .startTime(LocalDateTime.of(DATE, LocalTime.of(21, 0)))
      .event("PROG")
      .build()
    val visits = listOf(visit)
    whenever(
      scheduleRepository.getActivitiesAtLocation(
        -100L,
        DATE,
        DATE,
        "lastName",
        Order.ASC,
        false,
      ),
    ).thenReturn(visits)

    val results = schedulesService.getLocationEvents(-100L, "PROG", DATE, TimeSlot.ED, null, null)
    assertThat(results[0].offenderNo).isEqualTo("A10")
  }

  @Nested
  internal inner class getActivitiesAtAllLocations {
    @Test
    fun testGeActivitiesAtAllLocations_callsTheRepositoryWithTheCorrectParameters() {
      val today = LocalDate.now()
      val sortFields = "lastName,startTime"

      schedulesService.getActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM, sortFields, Order.ASC, true)

      verify(scheduleRepository)
        .getAllActivitiesAtAgency(
          "LEI",
          today,
          today,
          sortFields,
          Order.ASC,
          includeSuspended = true,
          onlySuspended = false,
        )
    }

    @Test
    fun testGeActivitiesAtAllLocations_appliesTimeSlotFiltering() {
      val today = LocalDate.now()

      whenever(
        scheduleRepository.getAllActivitiesAtAgency(
          eq("LEI"),
          eq(today),
          eq(today),
          eq("lastName"),
          eq(Order.ASC),
          ArgumentMatchers.eq(false),
          ArgumentMatchers.eq(false),
        ),
      )
        .thenReturn(
          listOf(
            PrisonerSchedule
              .builder()
              .startTime(LocalDateTime.now().withHour(23))
              .endTime(LocalDateTime.now().withHour(23))
              .locationId(3L)
              .bookingId(1L)
              .eventLocationId(3L)
              .eventId(2L)
              .build(),
            PrisonerSchedule
              .builder()
              .startTime(LocalDateTime.now().withHour(11))
              .endTime(LocalDateTime.now().withHour(11))
              .locationId(3L)
              .bookingId(1L)
              .eventLocationId(3L)
              .eventId(3L)
              .build(),
          ),
        )

      val activities =
        schedulesService.getActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM, null, Order.ASC, false)

      assertThat(activities).hasSize(1)
    }

    @Test
    fun testGeActivitiesAtAllLocations_CallsTheRepositoryWithTheCorrectParameters() {
      val from = LocalDate.now()
      val to = LocalDate.now().plusDays(1)

      val sortFields = "lastName,startTime"

      schedulesService.getActivitiesAtAllLocations("LEI", from, to, TimeSlot.AM, sortFields, Order.ASC, false)

      verify(scheduleRepository)
        .getAllActivitiesAtAgency(
          "LEI",
          from,
          to,
          sortFields,
          Order.ASC,
          includeSuspended = false,
          onlySuspended = false,
        )
    }

    @Test
    fun testGeActivitiesAtAllLocations_UseFromDate_WhenToDateIsNull() {
      val from = LocalDate.now().plusDays(-10)
      val sortFields = "lastName,startTime"

      schedulesService.getActivitiesAtAllLocations("LEI", from, null, TimeSlot.AM, sortFields, Order.ASC, false)

      verify(scheduleRepository)
        .getAllActivitiesAtAgency(
          "LEI",
          from,
          from,
          sortFields,
          Order.ASC,
          includeSuspended = false,
          onlySuspended = false,
        )
    }
  }

  @Nested
  internal inner class getSuspendedActivitiesAtAllLocations {
    @Test
    fun testGeActivitiesAtAllLocations_callsTheRepositoryWithTheCorrectParameters() {
      val today = LocalDate.now()
      val sortFields = "lastName"

      schedulesService.getSuspendedActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM)

      verify(scheduleRepository)
        .getAllActivitiesAtAgency(
          "LEI",
          today,
          today,
          sortFields,
          Order.ASC,
          includeSuspended = true,
          onlySuspended = true,
        )
    }

    @Test
    fun testGeActivitiesAtAllLocations_appliesTimeSlotFiltering() {
      val today = LocalDate.now()

      whenever(
        scheduleRepository.getAllActivitiesAtAgency(
          eq("LEI"),
          eq(today),
          eq(today),
          eq("lastName"),
          ArgumentMatchers.eq(
            Order.ASC,
          ),
          ArgumentMatchers.eq(true),
          ArgumentMatchers.eq(true),
        ),
      )
        .thenReturn(
          listOf(
            PrisonerSchedule
              .builder()
              .startTime(LocalDateTime.now().withHour(23))
              .endTime(LocalDateTime.now().withHour(23))
              .locationId(3L)
              .bookingId(1L)
              .eventLocationId(3L)
              .eventId(2L)
              .build(),
            PrisonerSchedule
              .builder()
              .startTime(LocalDateTime.now().withHour(11))
              .endTime(LocalDateTime.now().withHour(11))
              .locationId(3L)
              .bookingId(1L)
              .eventLocationId(3L)
              .eventId(3L)
              .build(),
          ),
        )

      val activities = schedulesService.getSuspendedActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM)

      assertThat(activities).hasSize(1)
    }

    @Test
    fun testGeActivitiesAtAllLocations_CallsTheRepositoryWithTheCorrectParameters() {
      val from = LocalDate.now()
      val to = LocalDate.now().plusDays(1)

      val sortFields = "lastName"

      schedulesService.getSuspendedActivitiesAtAllLocations("LEI", from, to, TimeSlot.AM)

      verify(scheduleRepository)
        .getAllActivitiesAtAgency("LEI", from, to, sortFields, Order.ASC, includeSuspended = true, onlySuspended = true)
    }

    @Test
    fun testGeActivitiesAtAllLocations_UseFromDate_WhenToDateIsNull() {
      val from = LocalDate.now().plusDays(-10)
      val sortFields = "lastName"

      schedulesService.getSuspendedActivitiesAtAllLocations("LEI", from, null, TimeSlot.AM)

      verify(scheduleRepository)
        .getAllActivitiesAtAgency(
          "LEI",
          from,
          from,
          sortFields,
          Order.ASC,
          includeSuspended = true,
          onlySuspended = true,
        )
    }
  }

  @Test
  fun testCallsToGetVisits_AreBatched() {
    val offenders = (1..1000).map { it.toString() }
    schedulesService.getVisits("LEI", offenders, LocalDate.now(), TimeSlot.AM)

    verify(scheduleRepository, times(2)).getVisits(
      anyString(),
      anyList(),
      any(),
    )
  }

  @Test
  fun testCallsToGetAppointments_AreBatched() {
    val offenders = (1..1000).map { it.toString() }
    schedulesService.getAppointments("LEI", offenders, LocalDate.now(), TimeSlot.AM)

    verify(scheduleRepository, times(2)).getAppointments(
      anyString(),
      anyList(),
      any(),
    )
  }

  @Test
  fun testCallsToGetActivities_AreBatched() {
    val offenders = (1..1000).map { it.toString() }
    schedulesService.getActivitiesByEventIds("LEI", offenders, LocalDate.now(), TimeSlot.AM, true)

    verify(scheduleRepository, times(2)).getActivities(
      anyString(),
      anyList(),
      any(),
    )
  }

  @Test
  fun testCallsToGetCourtEvents_AreBatched() {
    val offenders = (1..1000).map { it.toString() }
    schedulesService.getCourtEvents("LEI", offenders, LocalDate.now(), TimeSlot.AM)

    verify(scheduleRepository, times(2))
      .getCourtEvents(anyList(), any())
  }

  @Test
  fun testCallsToGetExternalTransfers_AreBatched() {
    val offenders = (1..1000).map { it.toString() }
    schedulesService.getExternalTransfers("LEI", offenders, LocalDate.now())

    verify(scheduleRepository, times(2)).getExternalTransfers(
      any(),
      anyList(),
      any(),
    )
  }

  @Test
  fun testLocationIdIsValidated_OnGetLocationActivity() {
    val locationId = -1L

    schedulesService.getActivitiesAtLocation(locationId, LocalDate.now(), TimeSlot.AM, "", Order.ASC, false)

    verify(locationService).getLocation(locationId)
  }

  @Test
  fun testGetLocationActivity_callsTheRepositoryWithTheCorrectParameters() {
    val locationId = -1L
    val today = LocalDate.now()
    val sortFields = "lastName,startTime"

    schedulesService.getActivitiesAtLocation(locationId, today, TimeSlot.AM, sortFields, Order.ASC, true)

    verify(scheduleRepository)
      .getActivitiesAtLocation(locationId, today, today, sortFields, Order.ASC, true)
  }

  @Test
  fun testGetLocationActivity_appliesTimeSlotFiltering() {
    val today = LocalDate.now()

    whenever(
      scheduleRepository.getActivitiesAtLocation(
        anyLong(),
        any(),
        any(),
        anyString(),
        any(),
        anyBoolean(),
      ),
    )
      .thenReturn(
        listOf(
          PrisonerSchedule
            .builder()
            .startTime(LocalDateTime.now().withHour(23))
            .endTime(LocalDateTime.now().withHour(23))
            .build(),
          PrisonerSchedule
            .builder()
            .startTime(LocalDateTime.now().withHour(11))
            .endTime(LocalDateTime.now().withHour(11))
            .build(),
        ),
      )

    val activities = schedulesService
      .getActivitiesAtLocation(1L, today, TimeSlot.AM, null, Order.ASC, false)

    assertThat(activities).hasSize(1)
  }

  @Test
  fun testBatchGetScheduledActivities() {
    whenever(scheduledActivityRepository.findAllByEventIdIn(any()))
      .thenReturn(mutableListOf())

    val eventIds = (1..1000).map { it.toLong() }

    schedulesService.getActivitiesByEventIds("LEI", eventIds)

    verify(scheduledActivityRepository, times(2))
      .findAllByEventIdIn(any())
  }

  @Test
  fun getCountActivities() {
    whenever(
      scheduledActivityRepository.getActivities(
        any(),
        any(),
        any(),
      ),
    ).thenReturn(
      listOf(
        PrisonerActivityImpl(-1L, "Y", "ALLOC", null, null, "2022-02-23T10:20:30"), // null is not suspended
        PrisonerActivityImpl(-1L, null, "ALLOC", null, null, "2022-02-23T10:20:30"),
        PrisonerActivityImpl(
          -2L,
          "N",
          "ALLOC",
          null,
          null,
          "2022-02-23T10:20:30",
        ), // afternoon slot so won't be counted
        PrisonerActivityImpl(-3L, "N", "ALLOC", null, null, "2022-02-23T12:00:00"), // evening slot so will be counted
        PrisonerActivityImpl(
          -1L,
          "N",
          "ALLOC",
          null,
          null,
          "2022-02-23T17:05:00",
        ), // end date of program same as schedule date so will be counted
        PrisonerActivityImpl(
          -4L,
          "N",
          "END",
          "2022-03-05",
          "2022-03-05",
          "2022-02-23T10:20:30",
        ), // end date of program after schedule date so will be counted
        PrisonerActivityImpl(
          -5L,
          "N",
          "END",
          "2022-03-26",
          "2022-03-05",
          "2022-02-23T10:20:30",
        ), // end date of program before schedule date so won't be counted
        PrisonerActivityImpl(-6L, "N", "END", "2022-03-04", "2022-03-05", "2022-02-23T10:20:30"),
      ),
    )
    val startDate = LocalDate.parse("2022-02-23")
    val endDate = LocalDate.parse("2022-04-23")
    val counts = schedulesService.getCountActivities(
      "MDI",
      startDate,
      endDate,
      setOf(TimeSlot.AM, TimeSlot.ED),
      mapOf<Long?, Long?>(),
    )
    assertThat(counts).isEqualTo(PrisonerActivitiesCount(6, 1, 6))
    verify(scheduledActivityRepository).getActivities("MDI", startDate, endDate)
  }

  @Test
  fun getCountActivities_notRecorded() {
    whenever(
      scheduledActivityRepository.getActivities(
        any(),
        any(),
        any(),
      ),
    ).thenReturn(
      listOf(
        PrisonerActivityImpl(
          -1L,
          "Y",
          "END",
          "2022-02-23",
          "2022-02-20",
          "2022-02-23T10:20:30",
        ), // null is not suspended
        PrisonerActivityImpl(-1L, null, "ALLOC", null, null, "2022-02-23T10:20:30"),
        PrisonerActivityImpl(
          -2L,
          "N",
          "ALLOC",
          "2022-02-23",
          null,
          "2022-02-23T10:20:30",
        ), // afternoon slot so won't be counted
        PrisonerActivityImpl(
          -3L,
          "N",
          "ALLOC",
          "2022-02-23",
          null,
          "2022-02-23T12:00:00",
        ), // evening slot so should be counted
        PrisonerActivityImpl(-4L, "N", "ALLOC", "2022-02-23", null, "2022-02-23T17:05:00"),
      ),
    )
    val startDate = LocalDate.parse("2022-02-23")
    val endDate = LocalDate.parse("2022-04-23")
    val attendances = mapOf(
      -1L to 2L, // booking -1 has 2 scheduled attendances so will cancel each other out
      -4L to 1L, // booking -4 has 1 scheduled attendance
      -5L to 10L,
    ) // booking -5 doesn't have any scheduled attendances so will be ignored
    val counts = schedulesService.getCountActivities(
      "MDI",
      startDate,
      endDate,
      setOf(TimeSlot.AM, TimeSlot.ED),
      attendances,
    )
    assertThat(counts).isEqualTo(PrisonerActivitiesCount(4, 1, 1))
    verify(scheduledActivityRepository).getActivities("MDI", startDate, endDate)
  }

  @Test
  fun testGetScheduledTransfersForPrisoner() {
    val transfer = PrisonerPrisonSchedule.builder()
      .offenderNo("A10")
      .startTime(TIME_1000)
      .event("28")
      .eventStatus("SCH")
      .build()
    val transfers = listOf(transfer)
    whenever(scheduleRepository.getScheduledTransfersForPrisoner("A10"))
      .thenReturn(transfers)

    val results = schedulesService.getScheduledTransfersForPrisoner("A10")
    assertThat(results[0].offenderNo).isEqualTo("A10")
  }

  companion object {
    private val DATE: LocalDate = LocalDate.of(2018, Month.AUGUST, 31)
    private val TIME_1000: LocalDateTime = LocalDateTime.of(DATE, LocalTime.of(10, 0))
    private const val MAX_BATCH_SIZE = 500
  }
}
