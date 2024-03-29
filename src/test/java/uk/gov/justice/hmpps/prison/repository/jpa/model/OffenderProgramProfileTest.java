package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity.CourseActivityBuilder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile.OffenderProgramProfileBuilder;

import java.time.LocalDate;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class OffenderProgramProfileTest {
    private static final Random RANDOM_NUMBER_GENERATOR = new Random();

    @ParameterizedTest
    @MethodSource("programProfilesWithExpectedIsCurrentActivityAndIsCurrentWorkActivityResult")
    void currentWorkActivity(final OffenderProgramProfile programProfile, final boolean expectedIsCurrentActivityResult)
    {
        assertThat(programProfile.isCurrentActivity()).isEqualTo(expectedIsCurrentActivityResult);
    }

    private static Stream<Arguments> programProfilesWithExpectedIsCurrentActivityAndIsCurrentWorkActivityResult() {
        final var programProfileWithNoStartDate =
            programProfileBuilder()
                .startDate(null)
                .build();
        final var programProfileWithStartDateTomorrow =
            programProfileBuilder()
                .startDate(LocalDate.now().plusDays(1))
                .build();
        final var programProfileWithEndDateToday =
            programProfileBuilder()
                .endDate(LocalDate.now())
                .build();
        final var programProfileWithValidStartAndEndDate =
            programProfileBuilder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build();
        final var programProfileWithValidStartAndNoEndDate =
            programProfileBuilder()
                .startDate(LocalDate.now())
                .build();
        final var programProfileWithValidStartAndEndDateButHasEndStatus =
            programProfileBuilder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .programStatus("END")
                .build();
        final var programProfileWithoutCourseActivity =
            programProfile(null);
        final var courseActivityWithNoStartDate =
            courseActivityBuilder("NO START DATE")
                .scheduleStartDate(null)
                .build();
        final var courseActivityWithStartDateToday =
            courseActivityBuilder("START DATE TODAY")
                .scheduleStartDate(LocalDate.now())
                .build();
        final var courseActivityWithStartDateAfterToday =
            courseActivityBuilder("START DATE AFTER TODAY")
                .scheduleStartDate(LocalDate.now().plusDays(1))
                .build();
        final var courseActivityWithNoEndDate =
            courseActivityBuilder("NO END DATE")
                .scheduleEndDate(null)
                .build();
        final var courseActivityWithEndDateAfterToday =
            courseActivityBuilder("END DATE AFTER TODAY")
                .scheduleEndDate(LocalDate.now().plusDays(1))
                .build();
        final var courseActivityWithEndDateToday =
            courseActivityBuilder("END DATE TODAY")
                .scheduleEndDate(LocalDate.now())
                .build();
        final var courseActivityWithNoCode =
            courseActivityBuilder("NO CODE")
                .code(null)
                .build();
        final var courseActivityWithEDUCode =
            courseActivityBuilder("EDU CODE")
                .code("EDUEXAMPLE")
                .build();

        return Stream.of(
            arguments(programProfileWithNoStartDate, false),
            arguments(programProfileWithStartDateTomorrow, false),
            arguments(programProfileWithEndDateToday, false),
            arguments(programProfileWithValidStartAndEndDate, true),
            arguments(programProfileWithValidStartAndNoEndDate, true),
            arguments(programProfileWithValidStartAndEndDateButHasEndStatus, false),
            arguments(programProfileWithoutCourseActivity, false),
            arguments(programProfile(courseActivityWithNoStartDate), false),
            arguments(programProfile(courseActivityWithStartDateToday), true),
            arguments(programProfile(courseActivityWithStartDateAfterToday), false),
            arguments(programProfile(courseActivityWithNoEndDate), true),
            arguments(programProfile(courseActivityWithEndDateAfterToday), true),
            arguments(programProfile(courseActivityWithEndDateToday), false),
            arguments(programProfile(courseActivityWithNoCode), true),
            arguments(programProfile(courseActivityWithEDUCode), true)
        );
    }

    private static OffenderProgramProfileBuilder programProfileBuilder() {
        return OffenderProgramProfile.builder()
            .offenderProgramReferenceId(nextLong())
            .programStatus("ALLOC")
            .startDate(LocalDate.now().minusDays(10))
            .courseActivity(courseActivityBuilder(String.format("A random desc %d", nextLong())).build());
    }

    private static OffenderProgramProfile programProfile(final CourseActivity courseActivity) {
        return OffenderProgramProfile.builder()
            .offenderProgramReferenceId(nextLong())
            .programStatus("ALLOC")
            .startDate(LocalDate.now().minusDays(10))
            .courseActivity(courseActivity)
            .build();
    }

    private static CourseActivityBuilder courseActivityBuilder(final String description) {
        return CourseActivity.builder()
            .activityId(nextLong())
            .description(description)
            .code("VALID")
            .scheduleStartDate(LocalDate.now().minusDays(10));
    }

    private static long nextLong() {
        return RANDOM_NUMBER_GENERATOR.nextLong();
    }
}
