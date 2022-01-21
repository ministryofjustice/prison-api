package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RepeatTest {
    private static final LocalTime START_TIME = LocalTime.of(13, 50);
    private static final LocalDate WEDNESDAY = LocalDate.of(2019, 2, 27);
    private static final LocalDateTime WEDNESDAY_START = LocalDateTime.of(WEDNESDAY, START_TIME);
    private static final LocalDate JAN_31_2019 = LocalDate.of(2019, 1, 31);
    private static final LocalDateTime JAN_31_2019_START = LocalDateTime.of(JAN_31_2019, START_TIME);

    @Test
    public void singleElementStream() {
        assertThat(
                Repeat.builder()
                        .count(1)
                        .repeatPeriod(RepeatPeriod.DAILY)
                        .build()
                        .dateTimeStream(WEDNESDAY_START)
                        .toList())
                .containsExactly(WEDNESDAY_START);
    }

    @Test
    public void multiDay() {
        assertThat(
                Repeat.builder()
                        .count(7)
                        .repeatPeriod(RepeatPeriod.DAILY)
                        .build()
                        .dateTimeStream(WEDNESDAY_START)
                        .toList())
                .containsExactly(
                        WEDNESDAY_START, // Wednesday
                        WEDNESDAY_START.plusDays(1), // Thursday
                        WEDNESDAY_START.plusDays(2), // Friday
                        WEDNESDAY_START.plusDays(3), // Saturday
                        WEDNESDAY_START.plusDays(4), // Sunday
                        WEDNESDAY_START.plusDays(5), // Monday
                        WEDNESDAY_START.plusDays(6)  // Tuesday
                );
    }

    @Test
    public void multiWeekday() {
        assertThat(
                Repeat.builder()
                        .count(14)
                        .repeatPeriod(RepeatPeriod.WEEKDAYS)
                        .build()
                        .dateTimeStream(WEDNESDAY_START)
                        .toList())
                .containsExactly(
                        WEDNESDAY_START,               // Wednesday
                        WEDNESDAY_START.plusDays(1),   // Thursday
                        WEDNESDAY_START.plusDays(2),   // Friday
                        WEDNESDAY_START.plusDays(5),   // Monday
                        WEDNESDAY_START.plusDays(6),   // Tuesday
                        WEDNESDAY_START.plusDays(7),   // Wednesday
                        WEDNESDAY_START.plusDays(8),   // Thursday
                        WEDNESDAY_START.plusDays(9),   // Friday
                        WEDNESDAY_START.plusDays(12),  // Monday
                        WEDNESDAY_START.plusDays(13),  // Tuesday
                        WEDNESDAY_START.plusDays(14),  // Wednesday
                        WEDNESDAY_START.plusDays(15),  // Thursday
                        WEDNESDAY_START.plusDays(16),  // Friday
                        WEDNESDAY_START.plusDays(19)   // Monday
                );
    }

    @Test
    public void multiWeek() {
        assertThat(
                Repeat.builder()
                        .count(7)
                        .repeatPeriod(RepeatPeriod.WEEKLY)
                        .build()
                        .dateTimeStream(WEDNESDAY_START)
                        .toList())
                .containsExactly(
                        WEDNESDAY_START,
                        WEDNESDAY_START.plusDays(7),
                        WEDNESDAY_START.plusDays(7 * 2),
                        WEDNESDAY_START.plusDays(7 * 3),
                        WEDNESDAY_START.plusDays(7 * 4),
                        WEDNESDAY_START.plusDays(7 * 5),
                        WEDNESDAY_START.plusDays(7 * 6)
                );
    }

    @Test
    public void multiFortnight() {
        assertThat(
                Repeat.builder()
                        .count(7)
                        .repeatPeriod(RepeatPeriod.FORTNIGHTLY)
                        .build()
                        .dateTimeStream(WEDNESDAY_START)
                        .toList())
                .containsExactly(
                        WEDNESDAY_START,
                        WEDNESDAY_START.plusDays(14),
                        WEDNESDAY_START.plusDays(14 * 2),
                        WEDNESDAY_START.plusDays(14 * 3),
                        WEDNESDAY_START.plusDays(14 * 4),
                        WEDNESDAY_START.plusDays(14 * 5),
                        WEDNESDAY_START.plusDays(14 * 6)
                );
    }

    @Test
    public void multiMonth() {
        assertThat(
                Repeat.builder()
                        .count(7)
                        .repeatPeriod(RepeatPeriod.MONTHLY)
                        .build()
                        .dateTimeStream(JAN_31_2019_START)
                        .toList())
                .containsExactly(
                        JAN_31_2019_START,
                        LocalDateTime.of(LocalDate.of(2019, 2, 28), START_TIME),
                        LocalDateTime.of(LocalDate.of(2019, 3, 31), START_TIME),
                        LocalDateTime.of(LocalDate.of(2019, 4, 30), START_TIME),
                        LocalDateTime.of(LocalDate.of(2019, 5, 31), START_TIME),
                        LocalDateTime.of(LocalDate.of(2019, 6, 30), START_TIME),
                        LocalDateTime.of(LocalDate.of(2019, 7, 31), START_TIME)
                );
    }

    @Test
    public void noArgsConstructor() {
        new Repeat();
    }

    @Test
    public void allArgsConstructor() {
        new Repeat(RepeatPeriod.DAILY, 1);
    }
}
