package uk.gov.justice.hmpps.prison.repository

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.iterable.ThrowingExtractor
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.PrisonerPrisonSchedule
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.api.support.TimeSlot
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Objects
import java.util.function.Consumer
import java.util.stream.Stream

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
@WithMockAuthUser("ITAG_USER")
class ScheduleRepositoryTest {
  @Autowired
  private lateinit var repository: ScheduleRepository

  @Test
  fun testGetLocationActivities() {
    val date = LocalDate.parse("2015-12-11")
    val toDate = LocalDate.now()
    val results: List<PrisonerSchedule> =
      repository.getActivitiesAtLocation(-26L, date, toDate, "lastName,startTime", Order.ASC, false)
    assertThat(results).hasSize(32)
    assertPrisonerDetails(results[0])
    // assert at least 1 field from all results
    assertThat(results[1].startTime.toString()).isEqualTo("2017-09-12T13:00")
    assertThat(results[2].startTime.toString()).isEqualTo("2017-09-13T13:00")
    assertThat(results[3].startTime.toString()).isEqualTo("2017-09-14T13:00")
    assertThat(results[4].startTime.toString()).isEqualTo("2017-09-15T13:00")

    assertThat(results[1].bookingId.toString()).isEqualTo("-2")
    assertThat(results[12].bookingId.toString()).isEqualTo("-3")
    assertThat(results[16].bookingId.toString()).isEqualTo("-4")
    assertThat(results[23].bookingId.toString()).isEqualTo("-4")
    assertThat(results[24].bookingId.toString()).isEqualTo("-5")
    assertThat(results[31].bookingId.toString()).isEqualTo("-5")

    assertThat(results[5].lastName).isEqualTo("ANDERSON") // date today
    assertThat(results[6].lastName).isEqualTo("ANDERSON")

    assertThat(results[7].startTime.toString()).isEqualTo(
      LocalDate.now().format(
        DateTimeFormatter.ISO_DATE,
      ) + "T13:00",
    )
    assertThat(results[8].startTime.toString()).isEqualTo("2017-09-11T13:00")
    assertThat(results[9].startTime.toString()).isEqualTo("2017-09-12T13:00")
    assertThat(results[10].startTime.toString()).isEqualTo("2017-09-13T13:00")
    assertThat(results[11].startTime.toString()).isEqualTo("2017-09-14T13:00")

    assertThat(results[12].lastName).isEqualTo("BATES")
    assertThat(results[13].lastName).isEqualTo("BATES")
    assertThat(results[14].lastName).isEqualTo("BATES")
    assertThat(results[15].lastName).isEqualTo("BATES")
    assertThat(results[16].lastName).isEqualTo("CHAPLIN")
    assertThat(results[17].lastName).isEqualTo("CHAPLIN")
    assertThat(results[18].lastName).isEqualTo("CHAPLIN")
    assertThat(results[19].lastName).isEqualTo("CHAPLIN")
    assertThat(results[20].lastName).isEqualTo("CHAPLIN")
    assertThat(results[21].lastName).isEqualTo("CHAPLIN")
    assertThat(results[22].lastName).isEqualTo("CHAPLIN")
    assertThat(results[23].lastName).isEqualTo("CHAPLIN")
    assertThat(results[24].lastName).isEqualTo("MATTHEWS")
    assertThat(results[25].lastName).isEqualTo("MATTHEWS")
    assertThat(results[26].lastName).isEqualTo("MATTHEWS")
    assertThat(results[27].lastName).isEqualTo("MATTHEWS")
    assertThat(results[28].lastName).isEqualTo("MATTHEWS")
    assertThat(results[29].lastName).isEqualTo("MATTHEWS")
    assertThat(results[30].lastName).isEqualTo("MATTHEWS")
    assertThat(results[31].lastName).isEqualTo("MATTHEWS")

    results.forEach(
      Consumer { result: PrisonerSchedule ->
        assertThat(result.locationId).isEqualTo(-26L)
      },
    )
  }

  @Nested
  inner class GetLocationAppointments {
    @Test
    fun testGetLocationAppointments() {
      val date = LocalDate.parse("2015-12-11")
      val toDate = LocalDate.now()
      val results: List<PrisonerSchedule> =
        repository.getLocationAppointments(-28L, date, toDate, null, null)
      assertThat(results).hasSize(5)
      assertThat(results[0].lastName).isEqualTo("ANDERSON")
      assertThat(results[1].lastName).isEqualTo("BATES")
      assertThat(results[2].lastName).isEqualTo("MATTHEWS")
      assertThat(results[3].lastName).isEqualTo("MATTHEWS")
      assertThat(results[0].bookingId.toString()).isEqualTo("-2")
      assertThat(results[1].bookingId.toString()).isEqualTo("-3")
      assertThat(results[2].bookingId.toString()).isEqualTo("-5")

      results.forEach(
        Consumer { result: PrisonerSchedule ->
          assertThat(result.locationId).isEqualTo(-28L)
        },
      )
    }

    @Test
    fun testGetLocationAppointmentsForSameDay() {
      val date = LocalDate.parse("2017-08-15")
      val fromDate = LocalDate.parse("2017-08-15")
      val results: List<PrisonerSchedule> =
        repository.getLocationAppointments(-29L, date, fromDate, null, null)
      assertThat(results).hasSize(1)
      assertThat(results[0].startTime.toString()).isEqualTo("2017-08-15T14:30")
    }

    @Test
    fun testGetLocationDifferentDaysUpperMatching() {
      val date = LocalDate.parse("2017-08-12")
      val fromDate = LocalDate.parse("2017-08-15")
      val results: List<PrisonerSchedule> =
        repository.getLocationAppointments(-29L, date, fromDate, null, null)
      assertThat(results).hasSize(1)
      assertThat(results[0].startTime.toString()).isEqualTo("2017-08-15T14:30")
    }

    @Test
    fun testGetLocationDifferentDaysLowerMatching() {
      val date = LocalDate.parse("2017-08-15")
      val fromDate = LocalDate.parse("2017-08-17")
      val results: List<PrisonerSchedule> =
        repository.getLocationAppointments(-29L, date, fromDate, null, null)
      assertThat(results).hasSize(1)
      assertThat(results[0].startTime.toString()).isEqualTo("2017-08-15T14:30")
    }

    @Test
    fun testGetLocationAppointmentsForDifferentDays() {
      val date = LocalDate.parse("2017-08-15")
      val fromDate = LocalDate.parse("2017-09-16")
      val results: List<PrisonerSchedule> =
        repository.getLocationAppointments(-29L, date, fromDate, null, null)
      assertThat(results).hasSize(2)
      assertThat(results[0].startTime.toString()).isEqualTo("2017-09-15T14:30")
      assertThat(results[1].startTime.toString()).isEqualTo("2017-08-15T14:30")
    }
  }

  @Test
  fun testGetLocationVisits() {
    val date = LocalDate.parse("2015-12-11")
    val toDate = LocalDate.now()
    val results: List<PrisonerSchedule> = repository.getLocationVisits(-25L, date, toDate, null, null)
    assertThat(results).hasSize(6)
    assertThat(results[0].lastName).isEqualTo("ANDERSON")
    assertThat(results[1].lastName).isEqualTo("ANDERSON")
    assertThat(results[2].lastName).isEqualTo("ANDERSON")
    assertThat(results[3].lastName).isEqualTo("ANDERSON")
    assertThat(results[4].lastName).isEqualTo("BATES")
    assertThat(results[5].lastName).isEqualTo("MATTHEWS")
    assertThat(results[0].bookingId.toString()).isEqualTo("-1")
    assertThat(results[4].bookingId.toString()).isEqualTo("-3")
    assertThat(results[5].bookingId.toString()).isEqualTo("-5")

    results.forEach(
      Consumer { result: PrisonerSchedule ->
        assertThat(result.locationId).isEqualTo(-25L)
      },
    )
  }

  @Test
  fun testGetAppointments() {
    val date = LocalDate.parse("2017-05-12")
    val results: List<PrisonerSchedule> =
      repository.getAppointments("LEI", mutableListOf("A1234AB"), date)
    assertThat(results).hasSize(1)
    assertThat(results[0].eventId).isEqualTo(-16)
    assertThat(results[0].offenderNo).isEqualTo("A1234AB")
    assertThat(results[0].startTime).isEqualTo(LocalDateTime.parse("2017-05-12T09:30"))
    assertThat(results[0].event).isEqualTo("IMM")
    assertThat(results[0].locationId).isEqualTo(-28L)
    assertThat(results[0].eventLocation).isEqualTo("Visiting Room")
  }

  @Test
  fun testGetVisits() {
    val date = LocalDate.parse("2017-09-15")
    val results: List<PrisonerSchedule> = repository.getVisits("LEI", mutableListOf("A1234AA"), date)
    assertThat(results).hasSize(1)
    assertThat(results[0].offenderNo).isEqualTo("A1234AA")
    assertThat(results[0].startTime).isEqualTo(LocalDateTime.parse("2017-09-15T14:00"))
    assertThat(results[0].locationId).isEqualTo(-25L)
    assertThat(results[0].eventLocation).isEqualTo("Chapel")
  }

  @Test
  fun testGetActivities() {
    val date = LocalDate.parse("2017-09-15")
    val results: List<PrisonerSchedule> = repository.getActivities("LEI", mutableListOf("A1234AB", "A1234AD"), date)
    assertThat(results).hasSize(2)
    assertThat(results[0].offenderNo).isEqualTo("A1234AB")
    assertThat(results[0].excluded).isFalse()
    assertThat(results[0].startTime).isEqualTo(LocalDateTime.parse("2017-09-15T13:00"))
    assertThat(results[0].locationId).isEqualTo(-26L)
    assertThat(results[0].timeSlot).isEqualTo(TimeSlot.PM)
    assertThat(results[0].eventLocation).isEqualTo("Carpentry Workshop")

    assertThat(results[1].offenderNo).isEqualTo("A1234AD")
    assertThat(results[1].excluded).isFalse()
    assertThat(results[1].startTime).isEqualTo(LocalDateTime.parse("2017-09-15T13:00"))
    assertThat(results[1].locationId).isEqualTo(-26L)
    assertThat(results[1].timeSlot).isEqualTo(TimeSlot.PM)
    assertThat(results[1].eventLocation).isEqualTo("Carpentry Workshop")
  }

  @Test
  fun testGetActivitiesExcluded() {
    val date = LocalDate.parse("2017-09-11")
    val results: List<PrisonerSchedule> =
      repository.getActivities("LEI", mutableListOf("A1234AE"), date)
    assertThat(results)
      .extracting("offenderNo", "excluded", "locationId", "timeSlot", "startTime")
      .contains(
        Tuple("A1234AE", true, -25L, TimeSlot.AM, LocalDateTime.parse("2017-09-11T09:30:00")),
        Tuple("A1234AE", false, -26L, TimeSlot.PM, LocalDateTime.parse("2017-09-11T13:00:00")),
      )
  }

  @Test
  fun testGetCourtEvents() {
    val results: List<PrisonerSchedule> =
      repository.getCourtEvents(mutableListOf("A1234AA", "A1234AB"), LocalDate.parse("2017-02-17"))

    assertThat(results).hasSize(2)
    assertThat(results)
      .extracting("offenderNo", "eventType", "event", "eventDescription", "eventStatus", "startTime").contains(
        Tuple(
          "A1234AA",
          "COURT",
          "PR",
          "Production of Unsentenced Inmate at Cour",
          "EXP",
          LocalDateTime.parse("2017-02-17T17:00:00"),
        ),
        Tuple(
          "A1234AB",
          "COURT",
          "PR",
          "Production of Unsentenced Inmate at Cour",
          "COMP",
          LocalDateTime.parse("2017-02-17T18:00:00"),
        ),
      )
  }

  @Test
  fun testThatScheduledActivities_FromVariousActivityLocationsAreReturned() {
    val date = LocalDate.parse("2015-12-11")
    val toDate = LocalDate.now()
    val results: List<PrisonerSchedule> =
      repository.getAllActivitiesAtAgency(
        "LEI",
        date,
        toDate,
        "lastName,startTime",
        Order.ASC,
        includeSuspended = true,
        onlySuspended = false,
      )

    assertThat(results).extracting("locationId").contains(-25L, -26L, -27L)
  }

  @Test
  fun testGetAllActivitiesAtAgency() {
    val date = LocalDate.parse("2015-12-11")
    val toDate = LocalDate.now()
    val results: List<PrisonerSchedule> =
      repository.getAllActivitiesAtAgency(
        "LEI",
        date,
        toDate,
        "lastName,startTime",
        Order.ASC,
        includeSuspended = true,
        onlySuspended = false,
      )
    assertThat(results).hasSize(91)

    assertThat(results)
      .extracting<@NotNull Long, RuntimeException>(ThrowingExtractor { obj: PrisonerSchedule -> obj.locationId })
      .containsOnly(-26L, -27L, -25L)
    assertThat(results)
      .extracting<@NotBlank String, RuntimeException>(ThrowingExtractor { obj: PrisonerSchedule -> obj.event })
      .containsOnly("CHAP", "EDUC")

    // Check the offenders returned have the expected booking ids
    // -35L is someone at a different agency (simulating being transferred)
    // but who was allocated to a program at LEI during the specified time period
    // -40L is someone with a suspended schedule
    assertThat(results)
      .extracting<Long, RuntimeException>(ThrowingExtractor { obj: PrisonerSchedule -> obj.bookingId })
      .containsOnly(-1L, -2L, -3L, -4L, -5L, -6L, -35L, -40L)
    // Get offender cell locations. -1L and -3L share a cell.
    assertThat(results)
      .extracting<@NotBlank String, RuntimeException>(ThrowingExtractor { obj: PrisonerSchedule -> obj.cellLocation })
      .containsOnly("LEI-A-1-1", "LEI-A-1-2", "LEI-H-1-5", "LEI-A-1", "LEI-A-1-10", "MDI-1-1-001", "SYI-A-2-1")

    // Assert it return both suspended and not suspended
    assertThat(results)
      .extracting<Boolean, RuntimeException>(ThrowingExtractor { obj: PrisonerSchedule -> obj.suspended })
      .containsOnly(true, false)
  }

  @Test
  fun testGetAllActivitiesAtAgencySuspendedOnly() {
    val date = LocalDate.parse("2015-12-11")
    val toDate = LocalDate.now()
    val results: List<PrisonerSchedule> =
      repository.getAllActivitiesAtAgency(
        "LEI",
        date,
        toDate,
        "lastName,startTime",
        Order.ASC,
        includeSuspended = true,
        onlySuspended = true,
      )
    assertThat(results).hasSize(10)

    assertThat(results)
      .extracting<Long, RuntimeException>(ThrowingExtractor { obj: PrisonerSchedule -> obj.locationId })
      .containsOnly(-27L)
    assertThat(results)
      .extracting<String, RuntimeException>(ThrowingExtractor { obj: PrisonerSchedule -> obj.event })
      .containsOnly("EDUC")

    // Check the offenders returned have the expected booking ids
    // -35L is someone at a different agency (simulating being transferred)
    // but who was allocated to a program at LEI during the specified time period
    // -40L is someone with a suspended schedule
    assertThat(results)
      .extracting<Long, RuntimeException>(ThrowingExtractor { obj: PrisonerSchedule -> obj.bookingId })
      .containsOnly(-6L, -40L)
    // Get offender cell locations. -1L and -3L share a cell.
    assertThat(results)
      .extracting<@NotBlank String, RuntimeException>(ThrowingExtractor { obj: PrisonerSchedule -> obj.cellLocation })
      .containsOnly("LEI-A-1-2", "SYI-A-2-1")

    // Assert it only suspended
    assertThat(results)
      .extracting<Boolean, RuntimeException>(ThrowingExtractor { obj: PrisonerSchedule -> obj.suspended })
      .containsOnly(true)
  }

  @Test
  fun testScheduledActivity_ForAGivenDateRangeAreReturned() {
    val fromDate = LocalDate.of(2017, 9, 11)
    val toDate = LocalDate.of(2017, 9, 12)

    val activities: List<PrisonerSchedule> = repository.getAllActivitiesAtAgency(
      agencyId = "LEI",
      fromDate = fromDate,
      toDate = toDate,
      orderByFields = "lastName,startTime",
      order = Order.ASC,
      includeSuspended = false,
      onlySuspended = false,
    )

    assertThat(
      Objects.requireNonNull(activities)
        .stream()
        .flatMap { event: PrisonerSchedule ->
          Stream.of(
            event.startTime,
            event.endTime,
          )
        }
        .allMatch { date: LocalDateTime ->
          date.toLocalDate().isEqual(fromDate) || date.toLocalDate().isEqual(toDate)
        },
    )
      .isTrue()
  }

  @Test
  fun testSuspendedAre_NotReturned() {
    val fromDate = LocalDate.of(1985, 1, 1)
    val toDate = LocalDate.of(1985, 1, 1)

    val activities: List<PrisonerSchedule> =
      repository.getActivitiesAtLocation(-27L, fromDate, toDate, "lastName,startTime", Order.ASC, false)

    assertThat(activities).hasSize(0)
  }

  @Test
  fun testSuspendedActivity_Returned() {
    val fromDate = LocalDate.of(1985, 1, 1)
    val toDate = LocalDate.of(1985, 1, 1)
    val activities: List<PrisonerSchedule> =
      repository.getActivitiesAtLocation(-27L, fromDate, toDate, "lastName,startTime", Order.ASC, true)

    assertThat(activities).hasSize(1)
    assertThat(activities)
      .extracting("offenderNo", "event", "eventDescription", "eventStatus", "startTime", "suspended").contains(
        Tuple("A1234AF", "EDUC", "Education", null, LocalDateTime.parse("1985-01-01T13:10:00"), true),
      )
  }

  @Test
  fun testGetScheduledTransfersForPrisoner() {
    val results: List<PrisonerPrisonSchedule> = repository.getScheduledTransfersForPrisoner("A1234AC")

    assertThat(results).hasSize(4)
    assertThat(results).extracting(
      "offenderNo",
      "firstName",
      "lastName",
      "event",
      "eventDescription",
      "eventStatus",
      "startTime",
      "eventLocation",
    ).contains(
      Tuple(
        "A1234AC",
        "NORMAN",
        "BATES",
        "COMP",
        "Compassionate Transfer",
        "SCH",
        LocalDateTime.of(2019, 5, 1, 13, 0, 0),
        "Leeds",
      ),
    )
  }

  companion object {
    private fun assertPrisonerDetails(details: PrisonerSchedule) {
      assertThat(details.startTime.toString()).isEqualTo("2017-09-11T13:00")
      assertThat(details.endTime.toString()).isEqualTo("2017-09-11T15:00")

      assertThat(details.comment).isEqualTo("Woodwork")
      assertThat(details.event).isEqualTo("EDUC")
      assertThat(details.eventDescription).isEqualTo("Education")
      assertThat(details.offenderNo).isEqualTo("A1234AB")
      assertThat(details.firstName).isEqualTo("GILLIAN")
      assertThat(details.lastName).isEqualTo("ANDERSON")
      assertThat(details.cellLocation).isEqualTo("LEI-H-1-5")
    }
  }
}
