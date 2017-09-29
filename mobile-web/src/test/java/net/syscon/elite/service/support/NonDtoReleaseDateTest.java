package net.syscon.elite.service.support;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by andrewk on 21/09/2017.
 */
public class NonDtoReleaseDateTest {
    private static final int HIGHER_PRIORITY = -1;
    private static final int SAME_PRIORITY = 0;
    private static final int LOWER_PRIORITY = 1;
    private static final LocalDate RELEASE_DATE_NOW = LocalDate.now();
    private static final LocalDate EARLIER_RELEASE_DATE = RELEASE_DATE_NOW.minusDays(5);
    private static final LocalDate LATER_RELEASE_DATE = RELEASE_DATE_NOW.plusDays(5);

    @Test(expected = NullPointerException.class)
    public void testConstructorReleaseDateTypeRequired() {
        new NonDtoReleaseDate(null, LocalDate.now(), false);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorReleaseDateRequired() {
        new NonDtoReleaseDate(ReleaseDateType.AUTOMATIC_RELEASE_DATE, null, true);
    }

    // When both are overrides but have different dates, later release date has higher priority
    @Test
    public void testCompareToBothOverridesDiffDates() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.AUTOMATIC_RELEASE_DATE, RELEASE_DATE_NOW, true);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.CONDITIONAL_RELEASE_DATE, EARLIER_RELEASE_DATE, true);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY);

        List<NonDtoReleaseDate> dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are overrides but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToBothOverridesSameDates() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.NON_PAROLE_DATE, RELEASE_DATE_NOW, true);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.CONDITIONAL_RELEASE_DATE, RELEASE_DATE_NOW, true);

        assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY);  // Because CRD higher priority than NPD
        assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY); // Because NPD lower priority than CRD

        List<NonDtoReleaseDate> dateList = Arrays.asList(date1, date2);

        Collections.sort(dateList);

        assertThat(date2).isEqualTo(dateList.get(0));
    }

    // When different types with only one override but having different dates, later release date has higher priority
    @Test
    public void testCompareToOneOverrideDiffDates() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.NON_PAROLE_DATE, RELEASE_DATE_NOW, true);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.CONDITIONAL_RELEASE_DATE, LATER_RELEASE_DATE, false);

        assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY);

        List<NonDtoReleaseDate> dateList = Arrays.asList(date1, date2);

        Collections.sort(dateList);

        assertThat(date2).isEqualTo(dateList.get(0));
    }

    // When same type with only one override but having different dates, override has higher priority
    @Test
    public void testCompareToOneOverrideSameTypesDiffDates() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.CONDITIONAL_RELEASE_DATE, RELEASE_DATE_NOW, false);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.CONDITIONAL_RELEASE_DATE, EARLIER_RELEASE_DATE, true);

        assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY);

        List<NonDtoReleaseDate> dateList = Arrays.asList(date1, date2);

        Collections.sort(dateList);

        assertThat(date2).isEqualTo(dateList.get(0));
    }

    // When same type with only one override but having same dates, override has higher priority
    @Test
    public void testCompareToOneOverrideSameTypesSameDates() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.NON_PAROLE_DATE, RELEASE_DATE_NOW, true);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.NON_PAROLE_DATE, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY);

        List<NonDtoReleaseDate> dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated and different types with different dates, later release date has higher priority
    @Test
    public void testCompareToNoOverridesDiffTypesDiffDates() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.AUTOMATIC_RELEASE_DATE, RELEASE_DATE_NOW, false);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.NON_PAROLE_DATE, LATER_RELEASE_DATE, false);

        assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY);

        List<NonDtoReleaseDate> dateList = Arrays.asList(date1, date2);

        Collections.sort(dateList);

        assertThat(date2).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesARDvsCRD() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.AUTOMATIC_RELEASE_DATE, RELEASE_DATE_NOW, false);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.CONDITIONAL_RELEASE_DATE, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because ARD higher priority than CRD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because CRD lower priority than ARD

        List<NonDtoReleaseDate> dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesARDvsNPD() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.AUTOMATIC_RELEASE_DATE, RELEASE_DATE_NOW, false);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.NON_PAROLE_DATE, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because ARD higher priority than NPD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because NPD lower priority than ARD

        List<NonDtoReleaseDate> dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesARDvsPRRD() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.AUTOMATIC_RELEASE_DATE, RELEASE_DATE_NOW, false);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.POST_RECALL_RELEASE_DATE, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because ARD higher priority than PRRD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because PRRD lower priority than ARD

        List<NonDtoReleaseDate> dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesCRDvsNPD() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.CONDITIONAL_RELEASE_DATE, RELEASE_DATE_NOW, false);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.NON_PAROLE_DATE, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because CRD higher priority than NPD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because NPD lower priority than CRD

        List<NonDtoReleaseDate> dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesCRDvsPRRD() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.CONDITIONAL_RELEASE_DATE, RELEASE_DATE_NOW, false);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.POST_RECALL_RELEASE_DATE, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because CRD higher priority than PRRD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because PRRD lower priority than CRD

        List<NonDtoReleaseDate> dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesNPDvsPRRD() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.NON_PAROLE_DATE, RELEASE_DATE_NOW, false);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.POST_RECALL_RELEASE_DATE, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because NPD higher priority than PRRD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because PRRD lower priority than NPD

        List<NonDtoReleaseDate> dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    @Test
    public void testCompareToIdenticalObjects() throws Exception {
        NonDtoReleaseDate date1 = new NonDtoReleaseDate(ReleaseDateType.AUTOMATIC_RELEASE_DATE, RELEASE_DATE_NOW, false);
        NonDtoReleaseDate date2 = new NonDtoReleaseDate(ReleaseDateType.AUTOMATIC_RELEASE_DATE, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(SAME_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(SAME_PRIORITY);
    }
}