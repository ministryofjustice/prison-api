package net.syscon.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class CalcDateRanges {
    private static final int MAX_YEARS = 10;
    private final Date dobDateFrom;
    private final Date dobDateTo;

    public CalcDateRanges(Date dob, Date dobFrom, Date dobTo) {
        if (dob != null) {
            dobDateFrom = dob;
            dobDateTo = dob;
        } else if (dobFrom != null && dobTo == null) {
            dobDateFrom = dobFrom;
            dobDateTo = adjustYears(dobFrom, MAX_YEARS);
        } else if (dobFrom == null && dobTo != null) {
            dobDateFrom = adjustYears(dobTo, -MAX_YEARS);
            dobDateTo = dobTo;
        } else if (dobFrom != null) {
            dobDateFrom = dobFrom;
            if (isGreaterThanYearSpan(dobFrom, dobTo, MAX_YEARS)) {
                dobDateTo = adjustYears(dobFrom, MAX_YEARS);
            } else {
                dobDateTo = dobTo;
            }
        } else {
            dobDateFrom = null;
            dobDateTo = null;
        }
    }

    public Date getDobDateFrom() {
        return dobDateFrom;
    }

    public Date getDobDateTo() {
        return dobDateTo;
    }

    private boolean isGreaterThanYearSpan(Date fromDate, Date toDate, int maxYearSpan) {
        final LocalDate fromLocal = LocalDate.ofEpochDay(fromDate.getTime());
        final LocalDate toLocal = LocalDate.ofEpochDay(toDate.getTime());
        return fromLocal.until(toLocal, ChronoUnit.YEARS) > maxYearSpan;
    }

    private Date adjustYears(Date startDate, int years) {
        final LocalDate fromLocal = LocalDate.ofEpochDay(startDate.getTime());
        final LocalDate toLocal = years < 0 ? fromLocal.minusYears(years) : fromLocal.plusYears(years);
        return Date.from(toLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public boolean hasDobRange() {
        return dobDateFrom != null && dobDateTo != null;
    }
}