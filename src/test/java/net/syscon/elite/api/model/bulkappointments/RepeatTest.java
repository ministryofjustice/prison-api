package net.syscon.elite.api.model.bulkappointments;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RepeatTest {
    private static final LocalDateTime START = LocalDateTime.of(2019, 2, 27, 13, 50); // Wednesday

    @Test
    public void singleElementStream() {
        assertThat(
                Repeat.builder()
                        .count(1)
                        .repeatPeriod(RepeatPeriod.DAILY)
                        .build()
                        .dateTimeStream(START)
                        .collect(Collectors.toList()))
                .containsExactly(START);
    }

    @Test
    public void multipleElementStream() {
        assertThat(
                Repeat.builder()
                        .count(7)
                        .repeatPeriod(RepeatPeriod.WEEKDAYS)
                        .build()
                        .dateTimeStream(START)
                        .collect(Collectors.toList()))
                .containsExactly(
                        START, // Wednesday
                        START.plusDays(1), // Thursday
                        START.plusDays(2), // Friday
                        START.plusDays(5), // Monday
                        START.plusDays(6), // Tuesday
                        START.plusDays(7), // Wednesday
                        START.plusDays(8)  // Thursday
                );
    }

    @Test
    public void emptyInterval() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.DAILY)
                        .count(1)
                        .build()
                        .duration()
        ).isEqualTo(Duration.ZERO);

    }

    @Test
    public void dailyFor366() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.DAILY)
                        .count(366)
                        .build()
                        .duration()
        ).isLessThanOrEqualTo(Duration.ofDays(365));
    }

    @Test
    public void weekdaysFor52WeeksAndADay() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.WEEKDAYS)
                        .count(5 * 52 + 1)
                        .build()
                        .duration()
        ).isLessThanOrEqualTo(Duration.ofDays(365));
    }


    @Test
    public void weeklyFor53() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.WEEKLY)
                        .count(53)
                        .build()
                        .duration()
        ).isLessThanOrEqualTo(Duration.ofDays(365));
    }

    @Test
    public void fortnightlyFor27() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.FORTNIGHTLY)
                        .count(27)
                        .build()
                        .duration()
        ).isLessThanOrEqualTo(Duration.ofDays(365));
    }

    @Test
    public void monthlyFor13() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.MONTHLY)
                        .count(13)
                        .build()
                        .duration()
        ).isLessThanOrEqualTo(Duration.ofDays(365));
    }

    @Test
    public void dailyFor367() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.DAILY)
                        .count(367)
                        .build()
                        .duration()
        ).isGreaterThan(Duration.ofDays(365));
    }

    @Test
    public void weekdaysFor53Weeks() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.WEEKDAYS)
                        .count(5 * 53)
                        .build()
                        .duration()
        ).isGreaterThan(Duration.ofDays(365));
    }


    @Test
    public void weeklyFor54() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.WEEKLY)
                        .count(54)
                        .build()
                        .duration()
        ).isGreaterThan(Duration.ofDays(365));
    }

    @Test
    public void fortnightlyFor28() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.FORTNIGHTLY)
                        .count(28)
                        .build()
                        .duration()
        ).isGreaterThan(Duration.ofDays(365));
    }

    @Test
    public void monthlyFor14() {
        assertThat(
                Repeat.builder()
                        .repeatPeriod(RepeatPeriod.MONTHLY)
                        .count(14)
                        .build()
                        .duration()
        ).isGreaterThan(Duration.ofDays(365));
    }
}
