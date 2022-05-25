package uk.gov.justice.hmpps.prison.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class CalcDateRangesTest {

    private static final int TEN_YEARS = 10;
    private CalcDateRanges testClass;

    @Test
    public void testDateRangeIsNullForNoDates() {
        testClass = new CalcDateRanges(null, null, null, TEN_YEARS);
        assertThat(testClass.getDateFrom()).isNull();
        assertThat(testClass.getDateTo()).isNull();
    }

    @Test
    public void testFromAndToDateSetForDobOnly() {
        final var dob = LocalDate.now().atStartOfDay().toLocalDate();
        testClass = new CalcDateRanges(dob, null, null, TEN_YEARS);
        assertThat(testClass.getDateFrom()).isEqualTo(dob);
        assertThat(testClass.getDateTo()).isEqualTo(dob);
    }

    @Test
    public void testDateRangeCalcLessThan10Years() {
        final var fromDate = LocalDate.now().atStartOfDay().toLocalDate();
        final var toDate = fromDate.plusYears(3);

        testClass = new CalcDateRanges(null, fromDate, toDate, TEN_YEARS);
        assertThat(testClass.getDateFrom()).isEqualTo(fromDate);
        assertThat(testClass.getDateTo()).isEqualTo(toDate);
    }

    @Test
    public void testDateRangeCalcEqual10Years() {
        final var fromDate = LocalDate.now().atStartOfDay().toLocalDate();
        final var toDate = fromDate.plusYears(TEN_YEARS);

        testClass = new CalcDateRanges(null, fromDate, toDate, TEN_YEARS);
        assertThat(testClass.getDateFrom()).isEqualTo(fromDate);
        assertThat(testClass.getDateTo()).isEqualTo(toDate);
    }

    @Test
    public void testDateRangeCalcMoreThan10Years() {
        final var fromDate = LocalDate.now().atStartOfDay().toLocalDate();
        final var toDate = fromDate.plusYears(30);

        testClass = new CalcDateRanges(null, fromDate, toDate, TEN_YEARS);
        assertThat(testClass.getDateFrom()).isEqualTo(fromDate);
        assertThat(testClass.getDateTo()).isNotEqualTo(toDate);
        assertThat(testClass.getDateTo()).isEqualTo(fromDate.plusYears(TEN_YEARS));
    }


    @Test
    public void testDateRangeCalcOnlyFromSpecified() {
        final var fromDate = LocalDate.now().atStartOfDay().toLocalDate();

        testClass = new CalcDateRanges(null, fromDate, null, TEN_YEARS);
        assertThat(testClass.getDateFrom()).isEqualTo(fromDate);
        assertThat(testClass.getDateTo()).isEqualTo(fromDate.plusYears(TEN_YEARS));
    }

    @Test
    public void testDateRangeCalcOnlyToSpecified() {
        final var toDate = LocalDate.now().atStartOfDay().toLocalDate();

        testClass = new CalcDateRanges(null, null, toDate, TEN_YEARS);
        assertThat(testClass.getDateTo()).isEqualTo(toDate);
        assertThat(testClass.getDateFrom()).isEqualTo(toDate.minusYears(TEN_YEARS));
    }

    @Test
    public void testStartTimeToTimeSlot() {
        final var dummy = LocalDate.of(2018, 10, 5);
        assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(0, 0)))).isEqualTo(TimeSlot.AM);
        assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(11, 59)))).isEqualTo(TimeSlot.AM);
        assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(12, 0)))).isEqualTo(TimeSlot.PM);
        assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(16, 59)))).isEqualTo(TimeSlot.PM);
        assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(17, 0)))).isEqualTo(TimeSlot.ED);
        assertThat(CalcDateRanges.startTimeToTimeSlot(LocalDateTime.of(dummy, LocalTime.of(23, 59)))).isEqualTo(TimeSlot.ED);
    }

    @ParameterizedTest
    @MethodSource
    public void testEventStartsInTimeslot(EventStartsInTimeslotData data) {
        assertThat(CalcDateRanges.eventStartsInTimeslot(data.getStartDateTime(), data.timeSlot)).isEqualTo(data.result);
    }

    @ParameterizedTest
    @MethodSource
    public void testEventStartsInTimeslots(EventStartsInTimeslotsData data) {
      assertThat(CalcDateRanges.eventStartsInTimeslots(data.getStartDateTime(), data.timeSlots)).isEqualTo(data.result);
    }

    private static Stream<EventStartsInTimeslotData> testEventStartsInTimeslot() {
        return Stream.of(
            new EventStartsInTimeslotData(0, 0, null, true),
            new EventStartsInTimeslotData(0, 0, TimeSlot.AM, true),
            new EventStartsInTimeslotData(11, 59, TimeSlot.AM, true),
            new EventStartsInTimeslotData(12, 0, TimeSlot.PM, true),
            new EventStartsInTimeslotData(16, 59, TimeSlot.PM, true),
            new EventStartsInTimeslotData(17, 0, TimeSlot.ED, true),
            new EventStartsInTimeslotData(23, 59, TimeSlot.ED, true),

            new EventStartsInTimeslotData(0, 0, TimeSlot.PM, false),
            new EventStartsInTimeslotData(0, 0, TimeSlot.ED, false),
            new EventStartsInTimeslotData(11, 59, TimeSlot.PM, false),
            new EventStartsInTimeslotData(11, 59, TimeSlot.ED, false),
            new EventStartsInTimeslotData(12, 0, TimeSlot.ED, false),
            new EventStartsInTimeslotData(12, 0, TimeSlot.AM, false),
            new EventStartsInTimeslotData(16, 59, TimeSlot.ED, false),
            new EventStartsInTimeslotData(16, 59, TimeSlot.AM, false),
            new EventStartsInTimeslotData(17, 0, TimeSlot.PM, false),
            new EventStartsInTimeslotData(17, 0, TimeSlot.AM, false),
            new EventStartsInTimeslotData(23, 59, TimeSlot.PM, false),
            new EventStartsInTimeslotData(23, 59, TimeSlot.AM, false)
        );
    }

    private static Stream<EventStartsInTimeslotsData> testEventStartsInTimeslots() {
        return Stream.concat(testEventStartsInTimeslot().map(s -> new EventStartsInTimeslotsData(s.hour, s.minute, s.timeSlot == null ? null : Set.of(s.timeSlot), s.result)),
            Stream.of(
                new EventStartsInTimeslotsData(0, 0, Set.of(), true),
                new EventStartsInTimeslotsData(11, 59, Set.of(TimeSlot.AM, TimeSlot.PM), true),
                new EventStartsInTimeslotsData(11, 59, Set.of(TimeSlot.PM, TimeSlot.ED), false),
                new EventStartsInTimeslotsData(17, 59, Set.of(TimeSlot.PM, TimeSlot.ED), true)
            )
        );
    }

    private record EventStartsInTimeslotData(int hour, int minute, TimeSlot timeSlot, boolean result) {
        public LocalDateTime getStartDateTime() {
            return LocalDateTime.of(2018, 10, 5, hour, minute, 0);
        }
    }

    private record EventStartsInTimeslotsData(int hour, int minute, Set<TimeSlot> timeSlots, boolean result) {
        public LocalDateTime getStartDateTime() {
           return LocalDateTime.of(2018, 10, 5, hour, minute, 0);
        }
    }
}
