package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import jakarta.validation.ValidationException;
import java.time.LocalDateTime;

public enum RepeatPeriod {
    DAILY() {
        @Override
        public LocalDateTime endDateTime(final LocalDateTime start, final long offsetEndDateTimeByThisNumberOfPeriodsFromStart) {
            return start.plusDays(offsetEndDateTimeByThisNumberOfPeriodsFromStart);
        }
    },

    WEEKDAYS() {
        @Override
        public LocalDateTime endDateTime(final LocalDateTime start, final long offsetEndDateTimeByThisNumberOfPeriodsFromStart) {
            final var dayOfWeek = start.getDayOfWeek().getValue(); // Monday == 1, Sunday == 7.
            if (dayOfWeek > 5) {
                throw new ValidationException("Weekend starts not allowed for WEEKDAY repeat period, but " + start + " is a Saturday or Sunday");
            }
            final var weeks = offsetEndDateTimeByThisNumberOfPeriodsFromStart / 5;
            final var remainder = offsetEndDateTimeByThisNumberOfPeriodsFromStart % 5;
            final var daysToIncrement = weeks * 7 + (remainder + dayOfWeek > 5 ? remainder + 2 : remainder);
            return start.plusDays(daysToIncrement);
        }
    },

    WEEKLY() {
        @Override
        public LocalDateTime endDateTime(final LocalDateTime start, final long offsetEndDateTimeByThisNumberOfPeriodsFromStart) {
            return start.plusWeeks(offsetEndDateTimeByThisNumberOfPeriodsFromStart);
        }
    },

    FORTNIGHTLY() {
        @Override
        public LocalDateTime endDateTime(final LocalDateTime start, final long offsetEndDateTimeByThisNumberOfPeriodsFromStart) {
            return start.plusWeeks(2 * offsetEndDateTimeByThisNumberOfPeriodsFromStart);
        }
    },

    MONTHLY() {
        @Override
        public LocalDateTime endDateTime(final LocalDateTime start, final long offsetEndDateTimeByThisNumberOfPeriodsFromStart) {
            return start.plusMonths(offsetEndDateTimeByThisNumberOfPeriodsFromStart);
        }
    };

    public abstract LocalDateTime endDateTime(LocalDateTime start, long offsetEndDateTimeByThisNumberOfPeriodsFromStart);
}
