package net.syscon.elite.api.model.bulkappointments;

import java.time.Period;

public enum RepeatPeriod {
    DAILY(Period.ofDays(1)),
    WEEKLY(Period.ofWeeks(1)),
    FORTNIGHTLY(Period.ofWeeks(2)),
    MONTHLY(Period.ofMonths(1));

    private final Period period;

    RepeatPeriod(Period period) {
        this.period = period;
    }

    public Period getPeriod() {return period; }
}
