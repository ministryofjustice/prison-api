package net.syscon.elite.api.model.bulkappointments;

import java.time.LocalDateTime;

public enum RepeatPeriod {
    DAILY() {
        @Override
        public LocalDateTime next(LocalDateTime date) {
            return date.plusDays(1);
        }
    },
    WEEKDAYS() {
        @Override
        public LocalDateTime next(LocalDateTime date) {
            switch (date.getDayOfWeek()) {
                case FRIDAY:
                    return date.plusDays(3);
                case SATURDAY:
                    return date.plusDays(2);
                default:
                    return date.plusDays(1);
            }
        }
    },
    WEEKLY() {
        @Override
        public LocalDateTime next(LocalDateTime date) {
            return date.plusWeeks(1);
        }
    },
    FORTNIGHTLY() {
        @Override
        public LocalDateTime next(LocalDateTime date) {
            return date.plusWeeks(2);
        }
    },
    MONTHLY() {
        @Override
        public LocalDateTime next(LocalDateTime date) {
            return date.plusMonths(1);
        }
    };

    public abstract LocalDateTime next(LocalDateTime date);
}
