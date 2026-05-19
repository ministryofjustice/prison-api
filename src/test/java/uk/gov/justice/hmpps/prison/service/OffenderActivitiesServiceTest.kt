package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary
import uk.gov.justice.hmpps.prison.api.model.OffenderAttendance
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.Attendance
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramEndReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProgramService
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AttendanceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import java.time.LocalDate

class OffenderActivitiesServiceTest {
  private val repository: OffenderProgramProfileRepository = mock()
  private val attendanceRepository: AttendanceRepository = mock()

  private var service = OffenderActivitiesService(repository, attendanceRepository)

  @Nested
  internal inner class GetStartedWorkActivitiesTest {
    @Test
    fun returnsCorrectApiObject() {
      val earliestEndDate = LocalDate.of(2020, 1, 1)

      val currentWorkActivity: OffenderProgramProfile = OffenderProgramProfile.builder()
        .offenderBooking(
          OffenderBooking.builder()
            .bookingId(EXAMPLE_BOOKING_ID)
            .build(),
        )
        .agencyLocation(
          AgencyLocation.builder()
            .id("MDI")
            .description("MOORLAND (HMP & YOI)")
            .build(),
        )
        .offenderProgramReferenceId(-6L)
        .programStatus("ALLOC")
        .startDate(LocalDate.of(2016, 11, 9))
        .courseActivity(
          CourseActivity.builder()
            .activityId(-1L)
            .description("Woodwork AM")
            .code("WOOD")
            .scheduleStartDate(LocalDate.of(2012, 2, 28))
            .build(),
        )
        .endReason(OffenderProgramEndReason("end code 1", "end description 1"))
        .endCommentText("end comment 1")
        .build()
      val endedWorkActivity: OffenderProgramProfile = OffenderProgramProfile.builder()
        .offenderBooking(
          OffenderBooking.builder()
            .bookingId(EXAMPLE_BOOKING_ID)
            .build(),
        )
        .agencyLocation(
          AgencyLocation.builder()
            .id("MDI")
            .description("MOORLAND (HMP & YOI)")
            .build(),
        )
        .offenderProgramReferenceId(-6L)
        .programStatus("END")
        .startDate(LocalDate.of(2016, 11, 9))
        .endDate(LocalDate.of(2021, 1, 1))
        .courseActivity(
          CourseActivity.builder()
            .activityId(-2L)
            .description("Woodwork PM")
            .code("WOOD")
            .scheduleStartDate(LocalDate.of(2012, 2, 28))
            .build(),
        )
        .endReason(OffenderProgramEndReason("end code 2", "end description 2"))
        .endCommentText("end comment 2")
        .build()

      val returnedOffenderProgramProfiles = PageImpl(
        listOf(currentWorkActivity, endedWorkActivity),
        PAGE_REQUEST,
        0,
      )
      whenever(
        repository.findByNomisIdAndProgramStatusAndEndDateAfter(
          EXAMPLE_OFFENDER_NO,
          mutableListOf("ALLOC", "END"),
          earliestEndDate,
          PAGE_REQUEST,
        ),
      )
        .thenReturn(returnedOffenderProgramProfiles)

      val workActivitiesApiObject = service.getStartedActivities(
        EXAMPLE_OFFENDER_NO,
        earliestEndDate,
        PAGE_REQUEST,
      )

      assertThat(workActivitiesApiObject).isEqualTo(
        PageImpl(
          listOf(
            OffenderActivitySummary.builder()
              .bookingId(EXAMPLE_BOOKING_ID)
              .agencyLocationId("MDI")
              .agencyLocationDescription("Moorland (HMP & YOI)")
              .description("Woodwork AM")
              .startDate(LocalDate.of(2016, 11, 9))
              .endReasonCode("end code 1")
              .endReasonDescription("end description 1")
              .endCommentText("end comment 1")
              .isCurrentActivity(true)
              .build(),
            OffenderActivitySummary.builder()
              .bookingId(EXAMPLE_BOOKING_ID)
              .agencyLocationId("MDI")
              .agencyLocationDescription("Moorland (HMP & YOI)")
              .description("Woodwork PM")
              .startDate(LocalDate.of(2016, 11, 9))
              .endDate(LocalDate.of(2021, 1, 1))
              .endReasonCode("end code 2")
              .endReasonDescription("end description 2")
              .endCommentText("end comment 2")
              .isCurrentActivity(false)
              .build(),
          ),
          PAGE_REQUEST,
          0,
        ),
      )
    }

    @Test
    fun handlesMinimalNonNullValues() {
      val earliestEndDate = LocalDate.of(2020, 1, 1)

      whenever(
        repository.findByNomisIdAndProgramStatusAndEndDateAfter(
          EXAMPLE_OFFENDER_NO,
          mutableListOf<String?>("ALLOC", "END"),
          earliestEndDate,
          PAGE_REQUEST,
        ),
      )
        .thenReturn(PageImpl(mutableListOf(), PAGE_REQUEST, 0))

      val workActivitiesApiObject = service.getStartedActivities(
        EXAMPLE_OFFENDER_NO,
        earliestEndDate,
        PAGE_REQUEST,
      )

      assertThat(workActivitiesApiObject.getContent()).isEmpty()
    }
  }

  @Nested
  internal inner class GetHistoricalAttendanciesTest {
    @Test
    fun returnsCorrectApiObject() {
      val earliestDate = LocalDate.of(2020, 1, 1)
      val latestDate = LocalDate.of(2021, 2, 2)

      val offenderBooking1: OffenderBooking = OffenderBooking.builder()
        .bookingId(100L)
        .active(true)
        .location(AgencyLocation.builder().id("LEI").build())
        .build()
      val offenderBooking2 = OffenderBooking
        .builder()
        .bookingId(200L)
        .active(true)
        .location(AgencyLocation.builder().id("LEI").build())
        .build()

      val courseActivity1 = CourseActivity
        .builder()
        .activityId(-1L)
        .code("CC1")
        .description("Test Description 1")
        .scheduleStartDate(LocalDate.of(2012, 2, 20))
        .prisonId("MDI")
        .build()
      val courseActivity2 = CourseActivity
        .builder()
        .activityId(-2L)
        .code("WOOD")
        .description("Test Description 2")
        .scheduleStartDate(LocalDate.of(2012, 2, 28))
        .prisonId("WWI")
        .build()

      val programService1 = ProgramService
        .builder()
        .programId(1L)
        .activity("Woodwork")
        .build()
      val programService2 = ProgramService
        .builder()
        .programId(2L)
        .activity("Substance misuse course")
        .build()

      whenever(
        attendanceRepository.findByEventDateBetweenAndOutcome(
          EXAMPLE_OFFENDER_NO,
          earliestDate,
          latestDate,
          null,
          PAGE_REQUEST,
        ),
      )
        .thenReturn(
          PageImpl(
            listOf(
              Attendance(
                1111L,
                courseActivity1,
                offenderBooking1,
                programService1,
                earliestDate,
                "outcome1",
                "comment1",
              ),
              Attendance(2222L, courseActivity2, offenderBooking2, programService2, latestDate, "outcome2", "comment2"),
            ),
          ),
        )

      val result =
        service.getHistoricalAttendancies(EXAMPLE_OFFENDER_NO, earliestDate, latestDate, null, PAGE_REQUEST)

      assertThat(result.getContent()).isEqualTo(
        listOf(
          OffenderAttendance
            .builder()
            .eventDate(earliestDate)
            .outcome("outcome1")
            .activity(programService1.activity)
            .description(courseActivity1.description)
            .code(courseActivity1.code)
            .prisonId("MDI")
            .bookingId(offenderBooking1.bookingId)
            .comment("comment1")
            .build(),
          OffenderAttendance
            .builder()
            .eventDate(latestDate)
            .outcome("outcome2")
            .activity(programService2.activity)
            .description(courseActivity2.description)
            .code(courseActivity2.code)
            .prisonId("WWI")
            .bookingId(offenderBooking2.bookingId)
            .comment("comment2")
            .build(),
        ),
      )
    }
  }

  companion object {
    private const val EXAMPLE_OFFENDER_NO = "A1234AA"
    private const val EXAMPLE_BOOKING_ID = -33L
    private val PAGE_REQUEST = PageRequest.of(0, 5)
  }
}
