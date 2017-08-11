package net.syscon.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class CalcDateRangesTest {

    private CalcDateRanges testClass;

    @Test
    public void testDateRangeIsNullForNoDates() {
        testClass = new CalcDateRanges(null, null,null, 10);
        assertThat(testClass.getDobDateFrom()).isNull();
        assertThat(testClass.getDobDateTo()).isNull();
    }

    @Test
    public void testFromAndToDateSetForDobOnly() {
        final Date dob = new Date();
        testClass = new CalcDateRanges(dob, null,null, 10);
        assertThat(testClass.getDobDateFrom()).isEqualTo(dob);
        assertThat(testClass.getDobDateTo()).isEqualTo(dob);
    }

    @Test
    public void testDateRangeCalcLessThan10Years() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusYears(3);

        final Date dobFrom = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        final Date dobTo = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        testClass = new CalcDateRanges(null, dobFrom, dobTo, 10);
        assertThat(testClass.getDobDateFrom()).isEqualTo(dobFrom);
        assertThat(testClass.getDobDateTo()).isEqualTo(dobTo);
    }

    @Test
    public void testDateRangeCalcEqual10Years() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusYears(10);

        final Date dobFrom = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        final Date dobTo = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        testClass = new CalcDateRanges(null, dobFrom, dobTo, 10);
        assertThat(testClass.getDobDateFrom()).isEqualTo(dobFrom);
        assertThat(testClass.getDobDateTo()).isEqualTo(dobTo);
    }

    @Test
    public void testDateRangeCalcMoreThan10Years() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusYears(30);

        final Date dobFrom = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        final Date dobTo = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        testClass = new CalcDateRanges(null, dobFrom, dobTo, 10);
        assertThat(testClass.getDobDateFrom()).isEqualTo(dobFrom);
        assertThat(testClass.getDobDateTo()).isNotEqualTo(dobTo);
        assertThat(testClass.getDobDateTo()).isEqualTo(Date.from(fromDate.plusYears(10).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }


    @Test
    public void testDateRangeCalcOnlyFromSpecified() {
        LocalDate fromDate = LocalDate.now();

        final Date dobFrom = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        testClass = new CalcDateRanges(null, dobFrom, null, 10);
        assertThat(testClass.getDobDateFrom()).isEqualTo(dobFrom);
        assertThat(testClass.getDobDateTo()).isEqualTo(Date.from(fromDate.plusYears(10).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    @Test
    public void testDateRangeCalcOnlyToSpecified() {
        LocalDate toDate = LocalDate.now();

        final Date dobTo = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        testClass = new CalcDateRanges(null, null, dobTo, 10);
        assertThat(testClass.getDobDateTo()).isEqualTo(dobTo);
        assertThat(testClass.getDobDateFrom()).isEqualTo(Date.from(toDate.minusYears(10).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
}
