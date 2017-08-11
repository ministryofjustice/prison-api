package net.syscon.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static java.time.temporal.ChronoUnit.YEARS;

public class CalcDateRanges {
    private final Date dobDateFrom;
    private final Date dobDateTo;

    public CalcDateRanges(Date dob, Date dobFrom, Date dobTo, int maxYears) {
        if (dob != null) {
            dobDateFrom = dob;
            dobDateTo = dob;
        } else if (dobFrom != null && dobTo == null) {
            dobDateFrom = dobFrom;
            dobDateTo = adjustYears(dobFrom, maxYears);
        } else if (dobFrom == null && dobTo != null) {
            dobDateFrom = adjustYears(dobTo, maxYears * -1);
            dobDateTo = dobTo;
        } else if (dobFrom != null) {
            dobDateFrom = dobFrom;
            if (isGreaterThanYearSpan(dobFrom, dobTo, maxYears)) {
                dobDateTo = adjustYears(dobFrom, maxYears);
            } else {
                dobDateTo = dobTo;
            }
        } else {
            dobDateFrom = null;
            dobDateTo = null;
        }
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public Date getDobDateFrom() {
        return dobDateFrom;
    }

    public Date getDobDateTo() {
        return dobDateTo;
    }

    private boolean isGreaterThanYearSpan(Date fromDate, Date toDate, int maxYearSpan) {
        long years = YEARS.between(toLocalDate(fromDate), toLocalDate(toDate));
        return years > maxYearSpan;
    }

    private Date adjustYears(Date startDate, int years) {
        final LocalDate fromLocal = toLocalDate(startDate);
        final LocalDate toLocal = years < 0 ? fromLocal.minusYears(Math.abs(years)) : fromLocal.plusYears(Math.abs(years));
        return Date.from(toLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public boolean hasDobRange() {
        return dobDateFrom != null && dobDateTo != null;
    }
}