package net.syscon.util;

import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class CalcDateRangesTest {

    private CalcDateRanges testClass;

    @Test
    public void testDateRangeIsNullForNoDates() {
        testClass = new CalcDateRanges(null, null,null);
        assertThat(testClass.getDobDateFrom()).isNull();
        assertThat(testClass.getDobDateTo()).isNull();
    }

    @Test
    public void testFromAndToDateSetForDobOnly() {
        final Date dob = new Date();
        testClass = new CalcDateRanges(dob, null,null);
        assertThat(testClass.getDobDateFrom()).isEqualTo(dob);
        assertThat(testClass.getDobDateTo()).isEqualTo(dob);
    }
}
