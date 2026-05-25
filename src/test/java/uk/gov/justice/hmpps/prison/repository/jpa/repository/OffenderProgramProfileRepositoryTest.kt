package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramEndReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
class OffenderProgramProfileRepositoryTest(
  @Autowired private val repository: OffenderProgramProfileRepository,
) {
  @Test
  fun getActivitiesByBookingIdAndProgramStatus() {
    val activities = repository.findByOffenderBooking_BookingIdAndProgramStatus(-3L, "ALLOC")

    activities.sortWith(Comparator { o1: OffenderProgramProfile, o2: OffenderProgramProfile -> (o2.offenderProgramReferenceId - o1.offenderProgramReferenceId).toInt() })
    assertThat(activities).usingRecursiveComparison()
      .ignoringFields("offenderBooking", "agencyLocation")
      .ignoringFieldsMatchingRegexes(".*createUserId", ".*createDatetime", ".*listSequence")
      .isEqualTo(
        listOf(
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-6L)
            .programStatus("ALLOC")
            .startDate(LocalDate.of(2016, 11, 9))
            .programId(-2L)
            .courseActivity(
              CourseActivity.builder()
                .activityId(-2L)
                .description("Woodwork")
                .code("WOOD")
                .prisonId("LEI")
                .scheduleStartDate(LocalDate.of(2012, 2, 28))
                .build(),
            )
            .build(),
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-9L)
            .programStatus("ALLOC")
            .startDate(LocalDate.of(2016, 11, 9))
            .programId(-5L)
            .courseActivity(
              CourseActivity.builder()
                .activityId(-5L)
                .description("Weeding")
                .code("FG1")
                .prisonId("LEI")
                .scheduleStartDate(LocalDate.of(2009, 7, 4))
                .build(),
            )
            .build(),
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-10L)
            .programStatus("ALLOC")
            .startDate(LocalDate.of(2016, 11, 9))
            .programId(-6L)
            .courseActivity(
              CourseActivity.builder()
                .activityId(-6L)
                .description("Address Testing")
                .code("ABS")
                .prisonId("LEI")
                .scheduleStartDate(LocalDate.of(2009, 7, 4))
                .build(),
            )
            .build(),
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-11L)
            .programStatus("ALLOC")
            .startDate(LocalDate.of(2016, 11, 9))
            .programId(-3L)
            .courseActivity(
              CourseActivity.builder()
                .activityId(-3L)
                .description("Substance misuse course")
                .code("SUBS")
                .prisonId("LEI")
                .scheduleStartDate(LocalDate.of(2011, 1, 4))
                .build(),
            )
            .endReason(OffenderProgramEndReason("SECDEC", "Security"))
            .endReasonCode("SECDEC")
            .endCommentText("Offender End Comment Text 3")
            .build(),
        ),
      )

    val locationIds = activities.map { it.agencyLocation.id }
    assertThat(locationIds).containsOnly("LEI")
  }

  @Test
  fun getActivitiesByBookingIdAndProgramStatus_ReturnsNothing() {
    val activities = repository.findByOffenderBooking_BookingIdAndProgramStatus(-3L, "WAIT")

    assertThat(activities).isEmpty()
  }

  @Test
  fun findByNomisIdAndProgramStatusAndEndDateAfter() {
    val activities = repository.findByNomisIdAndProgramStatusAndEndDateAfter(
      "A1234AC",
      listOf("ALLOC", "END"),
      LocalDate.of(2021, 1, 1),
      PageRequest.of(0, 10),
    )
      .sortedWith { o1: OffenderProgramProfile, o2: OffenderProgramProfile -> (o2.offenderProgramReferenceId - o1.offenderProgramReferenceId).toInt() }

    assertThat(activities).usingRecursiveComparison()
      .ignoringFields("offenderBooking", "agencyLocation")
      .ignoringFieldsMatchingRegexes(".*createUserId", ".*createDatetime", ".*listSequence")
      .isEqualTo(
        listOf(
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-6L)
            .programStatus("ALLOC")
            .startDate(LocalDate.of(2016, 11, 9))
            .programId(-2L)
            .courseActivity(
              CourseActivity.builder()
                .activityId(-2L)
                .description("Woodwork")
                .code("WOOD")
                .prisonId("LEI")
                .scheduleStartDate(LocalDate.of(2012, 2, 28))
                .build(),
            )
            .build(),
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-9L)
            .programStatus("ALLOC")
            .startDate(LocalDate.of(2016, 11, 9))
            .programId(-5L)
            .courseActivity(
              CourseActivity.builder()
                .activityId(-5L)
                .description("Weeding")
                .code("FG1")
                .prisonId("LEI")
                .scheduleStartDate(LocalDate.of(2009, 7, 4))
                .build(),
            )
            .build(),
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-10L)
            .programStatus("ALLOC")
            .startDate(LocalDate.of(2016, 11, 9))
            .programId(-6L)
            .courseActivity(
              CourseActivity.builder()
                .activityId(-6L)
                .description("Address Testing")
                .code("ABS")
                .prisonId("LEI")
                .scheduleStartDate(LocalDate.of(2009, 7, 4))
                .build(),
            )
            .build(),
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-11L)
            .programStatus("ALLOC")
            .startDate(LocalDate.of(2016, 11, 9))
            .programId(-3L)
            .courseActivity(
              CourseActivity.builder()
                .activityId(-3L)
                .description("Substance misuse course")
                .code("SUBS")
                .prisonId("LEI")
                .scheduleStartDate(LocalDate.of(2011, 1, 4))
                .build(),
            )
            .endReason(OffenderProgramEndReason("SECDEC", "Security"))
            .endReasonCode("SECDEC")
            .endCommentText("Offender End Comment Text 3")
            .build(),
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-14L)
            .programStatus("END")
            .startDate(LocalDate.of(2016, 11, 9))
            .endDate(LocalDate.now().plusDays(1))
            .programId(-4L)
            .courseActivity(
              CourseActivity.builder()
                .activityId(-4L)
                .description("Core classes")
                .code("CORE")
                .prisonId("LEI")
                .scheduleStartDate(LocalDate.of(2009, 7, 4))
                .build(),
            )
            .endReason(OffenderProgramEndReason("TRF", "Transferred Out"))
            .endReasonCode("TRF")
            .build(),
        ),
      )

    val locationIds = activities.map { it.agencyLocation.id }
    assertThat(locationIds).containsOnly("LEI")
  }

  @Test
  fun findByNomisIdAndProgramStatusAndEndDateAfter_OnlyFiltersOutEarlierEndDates() {
    val activities = repository.findByNomisIdAndProgramStatusAndEndDateAfter(
      "A1234AC",
      listOf("PLAN"),
      LocalDate.of(2021, 1, 1),
      PageRequest.of(0, 10),
    )
      .sortedWith { o1: OffenderProgramProfile, o2: OffenderProgramProfile -> (o2.offenderProgramReferenceId - o1.offenderProgramReferenceId).toInt() }

    assertThat(activities)
    assertThat(activities).usingRecursiveComparison()
      .ignoringFields("offenderBooking", "agencyLocation")
      .ignoringFieldsMatchingRegexes(".*createUserId", ".*createDatetime", ".*listSequence")
      .isEqualTo(
        listOf(
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-3101L)
            .programStatus("PLAN")
            .courseActivity(
              CourseActivity.builder()
                .activityId(-1L)
                .description("Chapel Cleaner")
                .code("CC1")
                .prisonId("LEI")
                .scheduleStartDate(LocalDate.of(2016, 8, 8))
                .build(),
            )
            .programId(-1L)
            .build(),
          OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-3102L)
            .programStatus("PLAN")
            .endDate(LocalDate.of(2021, 1, 1))
            .programId(-2L)
            .courseActivity(
              CourseActivity.builder()
                .activityId(-3001L)
                .description("Gym session 1")
                .code("ABS")
                .prisonId("BXI")
                .scheduleStartDate(LocalDate.of(2009, 7, 4))
                .build(),
            )
            .build(),
        ),
      )
  }

  @Test
  fun findByNomisIdAndProgramStatusAndEndDateAfter_ReturnsNothing() {
    val activities = repository.findByNomisIdAndProgramStatusAndEndDateAfter(
      "A1234AC",
      listOf("WAIT"),
      LocalDate.of(2020, 1, 1),
      Pageable.unpaged(),
    )

    assertThat(activities).isEmpty()
  }
}
