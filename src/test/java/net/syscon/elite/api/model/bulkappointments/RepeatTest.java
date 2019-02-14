package net.syscon.elite.api.model.bulkappointments;

import org.junit.Test;

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

}
