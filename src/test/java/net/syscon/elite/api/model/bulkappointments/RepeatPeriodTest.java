package net.syscon.elite.api.model.bulkappointments;

import org.junit.Test;

import javax.ws.rs.BadRequestException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class RepeatPeriodTest {
    private static final LocalDateTime SATURDAY = LocalDateTime.of(2019, 1, 5, 1, 1);
    private static final LocalDateTime SUNDAY = LocalDateTime.of(2019, 1, 6, 1, 1);

    @Test
    public void assertValidTestData() {
        assertThat(SATURDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(SUNDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
    }

    @Test(expected = BadRequestException.class)
    public void endDateTimeNotDefinedForSaturdayStart() {
        RepeatPeriod.WEEKDAYS.endDateTime(SATURDAY, 0);
    }

    @Test(expected = BadRequestException.class)
    public void endDateTimeNotDefinedForSundayStart() {
        RepeatPeriod.WEEKDAYS.endDateTime(SUNDAY, 0);
    }
}
