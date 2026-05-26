package uk.gov.justice.hmpps.prison.repository.jpa.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.Random
import java.util.stream.Stream

class OffenderProgramProfileTest {
  @ParameterizedTest
  @MethodSource("programProfilesWithExpectedIsCurrentActivityAndIsCurrentWorkActivityResult")
  fun currentWorkActivity(programProfile: OffenderProgramProfile, expectedIsCurrentActivityResult: Boolean) {
    assertThat(programProfile.isCurrentActivity()).isEqualTo(expectedIsCurrentActivityResult)
  }

  companion object {
    private val RANDOM_NUMBER_GENERATOR = Random()

    @JvmStatic
    private fun programProfilesWithExpectedIsCurrentActivityAndIsCurrentWorkActivityResult(): Stream<Arguments> {
      val programProfileWithNoStartDate: OffenderProgramProfile =
        programProfileBuilder()
          .startDate(null)
          .build()
      val programProfileWithStartDateTomorrow: OffenderProgramProfile =
        programProfileBuilder()
          .startDate(LocalDate.now().plusDays(1))
          .build()
      val programProfileWithEndDateToday: OffenderProgramProfile =
        programProfileBuilder()
          .endDate(LocalDate.now())
          .build()
      val programProfileWithValidStartAndEndDate: OffenderProgramProfile =
        programProfileBuilder()
          .startDate(LocalDate.now())
          .endDate(LocalDate.now().plusDays(1))
          .build()
      val programProfileWithValidStartAndNoEndDate: OffenderProgramProfile =
        programProfileBuilder()
          .startDate(LocalDate.now())
          .build()
      val programProfileWithValidStartAndEndDateButHasEndStatus: OffenderProgramProfile =
        programProfileBuilder()
          .startDate(LocalDate.now())
          .endDate(LocalDate.now().plusDays(1))
          .programStatus("END")
          .build()
      val programProfileWithoutCourseActivity: OffenderProgramProfile =
        programProfile(null)
      val courseActivityWithNoStartDate = courseActivityBuilder("NO START DATE")
        .scheduleStartDate(null)
        .build()
      val courseActivityWithStartDateToday = courseActivityBuilder("START DATE TODAY")
        .scheduleStartDate(LocalDate.now())
        .build()
      val courseActivityWithStartDateAfterToday = courseActivityBuilder("START DATE AFTER TODAY")
        .scheduleStartDate(LocalDate.now().plusDays(1))
        .build()
      val courseActivityWithNoEndDate = courseActivityBuilder("NO END DATE")
        .scheduleEndDate(null)
        .build()
      val courseActivityWithEndDateAfterToday = courseActivityBuilder("END DATE AFTER TODAY")
        .scheduleEndDate(LocalDate.now().plusDays(1))
        .build()
      val courseActivityWithEndDateToday = courseActivityBuilder("END DATE TODAY")
        .scheduleEndDate(LocalDate.now())
        .build()
      val courseActivityWithNoCode = courseActivityBuilder("NO CODE")
        .code(null)
        .build()
      val courseActivityWithEDUCode = courseActivityBuilder("EDU CODE")
        .code("EDUEXAMPLE")
        .build()

      return Stream.of(
        Arguments.arguments(programProfileWithNoStartDate, false),
        Arguments.arguments(programProfileWithStartDateTomorrow, false),
        Arguments.arguments(programProfileWithEndDateToday, false),
        Arguments.arguments(programProfileWithValidStartAndEndDate, true),
        Arguments.arguments(programProfileWithValidStartAndNoEndDate, true),
        Arguments.arguments(programProfileWithValidStartAndEndDateButHasEndStatus, false),
        Arguments.arguments(programProfileWithoutCourseActivity, false),
        Arguments.arguments(programProfile(courseActivityWithNoStartDate), false),
        Arguments.arguments(programProfile(courseActivityWithStartDateToday), true),
        Arguments.arguments(programProfile(courseActivityWithStartDateAfterToday), false),
        Arguments.arguments(programProfile(courseActivityWithNoEndDate), true),
        Arguments.arguments(programProfile(courseActivityWithEndDateAfterToday), true),
        Arguments.arguments(programProfile(courseActivityWithEndDateToday), false),
        Arguments.arguments(programProfile(courseActivityWithNoCode), true),
        Arguments.arguments(programProfile(courseActivityWithEDUCode), true),
      )
    }

    private fun programProfileBuilder() = OffenderProgramProfile.builder()
      .offenderProgramReferenceId(nextLong())
      .programStatus("ALLOC")
      .startDate(LocalDate.now().minusDays(10))
      .courseActivity(
        courseActivityBuilder(
          String.format(
            "A random desc %d",
            nextLong(),
          ),
        ).build(),
      )

    private fun programProfile(courseActivity: CourseActivity?): OffenderProgramProfile = OffenderProgramProfile.builder()
      .offenderProgramReferenceId(nextLong())
      .programStatus("ALLOC")
      .startDate(LocalDate.now().minusDays(10))
      .courseActivity(courseActivity)
      .build()

    private fun courseActivityBuilder(description: String?): CourseActivity.CourseActivityBuilder = CourseActivity.builder()
      .activityId(nextLong())
      .description(description)
      .code("VALID")
      .scheduleStartDate(LocalDate.now().minusDays(10))

    private fun nextLong(): Long = RANDOM_NUMBER_GENERATOR.nextLong()
  }
}
