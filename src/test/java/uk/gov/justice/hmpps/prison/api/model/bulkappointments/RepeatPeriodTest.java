package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import org.junit.jupiter.api.Test;

import jakarta.validation.ValidationException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RepeatPeriodTest {
    private static final LocalDateTime SATURDAY = LocalDateTime.of(2019, 1, 5, 1, 1);
    private static final LocalDateTime SUNDAY = LocalDateTime.of(2019, 1, 6, 1, 1);

    @Test
    public void assertValidTestData() {
        assertThat(SATURDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(SUNDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
    }

    @Test
    public void endDateTimeNotDefinedForSaturdayStart() {
        assertThatThrownBy(
                () -> RepeatPeriod.WEEKDAYS.endDateTime(SATURDAY, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Weekend starts not allowed for WEEKDAY repeat period, but 2019-01-05T01:01 is a Saturday or Sunday");
    }

    @Test
    public void endDateTimeNotDefinedForSundayStart() {
        assertThatThrownBy(() -> RepeatPeriod.WEEKDAYS.endDateTime(SUNDAY, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Weekend starts not allowed for WEEKDAY repeat period, but 2019-01-06T01:01 is a Saturday or Sunday");
    }
}
