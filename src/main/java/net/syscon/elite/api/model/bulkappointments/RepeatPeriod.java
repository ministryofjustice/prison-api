package net.syscon.elite.api.model.bulkappointments;

import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;

public enum RepeatPeriod {
    DAILY() {
        @Override
        public LocalDateTime endDateTime(LocalDateTime start, long numberOfPeriods) {
            return start.plusDays(numberOfPeriods);
        }
    },

    WEEKDAYS() {
        @Override
        public LocalDateTime endDateTime(LocalDateTime start, long numberOfPeriods) {
            int dayOfWeek = start.getDayOfWeek().getValue(); // Monday == 1, Sunday == 7.
            if (dayOfWeek > 5) {
                throw new BadRequestException("Weekend starts not allowed for WEEKDAY repeat period, but "+ start + " is a Saturday or Sunday");
            }
            long weeks = numberOfPeriods / 5;
            long remainder = numberOfPeriods % 5;
            long daysToIncrement = weeks * 7 + (remainder + dayOfWeek > 5 ? remainder + 2 : remainder);
            return start.plusDays(daysToIncrement);
        }
    },

    WEEKLY() {
        @Override
        public LocalDateTime endDateTime(LocalDateTime start, long numberOfPeriods) {
            return start.plusWeeks(numberOfPeriods);
        }
    },

    FORTNIGHTLY() {
        @Override
        public LocalDateTime endDateTime(LocalDateTime start, long numberOfPeriods) {
            return start.plusWeeks(2 * numberOfPeriods);
        }
    },

    MONTHLY() {
        @Override
        public LocalDateTime endDateTime(LocalDateTime start, long numberOfPeriods) {
            return start.plusMonths(numberOfPeriods);
        }
    };

    public abstract LocalDateTime endDateTime(LocalDateTime start, long numberOfPeriods);
}
