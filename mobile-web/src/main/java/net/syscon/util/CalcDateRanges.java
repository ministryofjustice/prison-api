package net.syscon.util;

import org.apache.commons.lang3.Range;

import java.time.LocalDate;
import java.util.Comparator;

import static java.time.temporal.ChronoUnit.YEARS;

public class CalcDateRanges {
    private static final Comparator<LocalDate> LOCAL_DATE_COMPARATOR =
            Comparator.comparingInt(LocalDate::getYear)
                    .thenComparingInt(LocalDate::getMonthValue)
                    .thenComparingInt(LocalDate::getDayOfMonth);

    private final LocalDate dateFrom;
    private final LocalDate dateTo;

    public CalcDateRanges(LocalDate date, LocalDate dateFrom, LocalDate dateTo, int maxYears) {
        if (date != null) {
            this.dateFrom = date;
            this.dateTo = date;
        } else if (dateFrom != null && dateTo == null) {
            this.dateFrom = dateFrom;
            this.dateTo = adjustYears(dateFrom, maxYears);
        } else if (dateFrom == null && dateTo != null) {
            this.dateFrom = adjustYears(dateTo, maxYears * -1);
            this.dateTo = dateTo;
        } else if (dateFrom != null) {
            this.dateFrom = dateFrom;
            if (isGreaterThanMaxYearsSpan(dateFrom, dateTo, maxYears)) {
                this.dateTo = adjustYears(dateFrom, maxYears);
            } else {
                this.dateTo = dateTo;
            }
        } else {
            this.dateFrom = null;
            this.dateTo = null;
        }
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    private boolean isGreaterThanMaxYearsSpan(LocalDate fromDate, LocalDate toDate, int maxYearsSpan) {
        long years = YEARS.between(fromDate, toDate);
        return years >= maxYearsSpan;
    }

    private LocalDate adjustYears(LocalDate fromLocal, int years) {
        return years < 0 ? fromLocal.minusYears(Math.abs(years)) : fromLocal.plusYears(Math.abs(years));
    }

    public boolean hasDateRange() {
        return dateFrom != null && dateTo != null;
    }

    public Range<LocalDate> getDateRange() {
        return hasDateRange() ? Range.between(dateFrom, dateTo, LOCAL_DATE_COMPARATOR) : null;
    }
}