package net.syscon.elite.api.model.bulkappointments;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class RepeatPeriodTest {
    private static final LocalDateTime TUESDAY = LocalDateTime.of(2019, 2, 26, 0, 0);
    private static final LocalDateTime WEDNESDAY = LocalDateTime.of(2019, 2, 27, 0, 0);
    private static final LocalDateTime THURSDAY = LocalDateTime.of(2019, 2, 28, 0, 0);
    private static final LocalDateTime FRIDAY = LocalDateTime.of(2019, 3, 1, 0, 0);
    private static final LocalDateTime SATURDAY = LocalDateTime.of(2019, 3, 2, 0, 0);
    private static final LocalDateTime SUNDAY = LocalDateTime.of(2019, 3, 3, 0, 0);
    private static final LocalDateTime MONDAY = LocalDateTime.of(2019, 3, 4, 0, 0);
    private static final LocalDateTime TUESDAY_1 = LocalDateTime.of(2019, 3, 5, 0, 0);

    @Test
    public void checkDayOfWeek() {
        assertThat(MONDAY.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(TUESDAY.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(WEDNESDAY.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(THURSDAY.getDayOfWeek()).isEqualTo(DayOfWeek.THURSDAY);
        assertThat(FRIDAY.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(SATURDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(SUNDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
        assertThat(TUESDAY_1.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
    }

    @Test
    public void daily() {
        assertThat(RepeatPeriod.DAILY.next(TUESDAY)).isEqualTo(WEDNESDAY);
        assertThat(RepeatPeriod.DAILY.next(WEDNESDAY)).isEqualTo(THURSDAY);
        assertThat(RepeatPeriod.DAILY.next(THURSDAY)).isEqualTo(FRIDAY);
        assertThat(RepeatPeriod.DAILY.next(FRIDAY)).isEqualTo(SATURDAY);
        assertThat(RepeatPeriod.DAILY.next(SATURDAY)).isEqualTo(SUNDAY);
        assertThat(RepeatPeriod.DAILY.next(SUNDAY)).isEqualTo(MONDAY);
        assertThat(RepeatPeriod.DAILY.next(MONDAY)).isEqualTo(TUESDAY_1);
    }

    @Test
    public void weekdays() {
        assertThat(RepeatPeriod.WEEKDAYS.next(TUESDAY)).isEqualTo(WEDNESDAY);
        assertThat(RepeatPeriod.WEEKDAYS.next(WEDNESDAY)).isEqualTo(THURSDAY);
        assertThat(RepeatPeriod.WEEKDAYS.next(THURSDAY)).isEqualTo(FRIDAY);
        assertThat(RepeatPeriod.WEEKDAYS.next(FRIDAY)).isEqualTo(MONDAY);
        assertThat(RepeatPeriod.WEEKDAYS.next(SATURDAY)).isEqualTo(MONDAY);
        assertThat(RepeatPeriod.WEEKDAYS.next(SUNDAY)).isEqualTo(MONDAY);
        assertThat(RepeatPeriod.WEEKDAYS.next(MONDAY)).isEqualTo(TUESDAY_1);
    }

    @Test
    public void weekly() {
        assertThat(RepeatPeriod.WEEKLY.next(TUESDAY)).isEqualTo(TUESDAY_1);
    }

    @Test
    public void fortnightly() {
        assertThat(RepeatPeriod.FORTNIGHTLY.next(TUESDAY)).isEqualTo(TUESDAY.plusDays(14));
    }

    @Test
    public void monthly() {
        assertThat(RepeatPeriod.MONTHLY.next(TUESDAY)).isEqualTo(TUESDAY.plusMonths(1));
    }

}
