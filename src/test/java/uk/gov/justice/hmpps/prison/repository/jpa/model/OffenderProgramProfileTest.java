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
    void currentWorkActivity(final OffenderProgramProfile programProfile, final boolean expectedIsCurrentActivityResult, final boolean expectedIsCurrentWorkActivityResult)
    {
        assertThat(programProfile.isCurrentActivity()).isEqualTo(expectedIsCurrentActivityResult);
        assertThat(programProfile.isCurrentWorkActivity()).isEqualTo(expectedIsCurrentWorkActivityResult);
    }

    @ParameterizedTest
    @MethodSource("programProfilesWithExpectedIsWorkActivityResult")
    void isWorkActivity(final OffenderProgramProfile programProfile, final boolean expectedIsWorkActivityResult)
    {
        assertThat(programProfile.isWorkActivity()).isEqualTo(expectedIsWorkActivityResult);
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
            arguments(programProfileWithNoStartDate, false, false),
            arguments(programProfileWithStartDateTomorrow, false, false),
            arguments(programProfileWithEndDateToday, false, false),
            arguments(programProfileWithValidStartAndEndDate, true, true),
            arguments(programProfileWithValidStartAndNoEndDate, true, true),
            arguments(programProfileWithValidStartAndEndDateButHasEndStatus, false, false),
            arguments(programProfileWithoutCourseActivity, false, false),
            arguments(programProfile(courseActivityWithNoStartDate), false, false),
            arguments(programProfile(courseActivityWithStartDateToday), true, true),
            arguments(programProfile(courseActivityWithStartDateAfterToday), false, false),
            arguments(programProfile(courseActivityWithNoEndDate), true, true),
            arguments(programProfile(courseActivityWithEndDateAfterToday), true, true),
            arguments(programProfile(courseActivityWithEndDateToday), false, false),
            arguments(programProfile(courseActivityWithNoCode), true, false),
            arguments(programProfile(courseActivityWithEDUCode), true, false)
        );
    }

    private static Stream<Arguments> programProfilesWithExpectedIsWorkActivityResult() {
        final var programProfileWithoutCourseActivity =
            programProfile(null);
        final var courseActivityWithNoCode =
            courseActivityBuilder("NO CODE")
                .code(null)
                .build();
        final var courseActivityWithEDUCode =
            courseActivityBuilder("EDU CODE")
                .code("EDUEXAMPLE")
                .build();
        final var courseActivityWithWorkCode =
            courseActivityBuilder("IND_CODE")
                .build();

        return Stream.of(
            arguments(programProfileWithoutCourseActivity, false),
            arguments(programProfile(courseActivityWithNoCode), false),
            arguments(programProfile(courseActivityWithEDUCode), false),
            arguments(programProfile(courseActivityWithWorkCode), true)
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

    private static CourseActivityBuilder courseActivityBuilder(String description) {
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
