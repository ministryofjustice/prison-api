package uk.gov.justice.hmpps.prison.service.support;

import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    public void testConstructorReleaseDateTypeRequired() {
        assertThatThrownBy(() -> new NonDtoReleaseDate(null, LocalDate.now(), false)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConstructorReleaseDateRequired() {
        assertThatThrownBy(() -> new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, null, true)).isInstanceOf(NullPointerException.class);
    }

    // When both are overrides but have different dates, later release date has higher priority
    @Test
    public void testCompareToBothOverridesDiffDates() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, true);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, EARLIER_RELEASE_DATE, true);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY);

        final var dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are overrides but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToBothOverridesSameDates() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, true);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, RELEASE_DATE_NOW, true);

        assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY);  // Because CRD higher priority than NPD
        assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY); // Because NPD lower priority than CRD

        final var dateList = Arrays.asList(date1, date2);

        Collections.sort(dateList);

        assertThat(date2).isEqualTo(dateList.get(0));
    }

    // When different types with only one override but having different dates, later release date has higher priority
    @Test
    public void testCompareToOneOverrideDiffDates() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, true);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, LATER_RELEASE_DATE, false);

        assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY);

        final var dateList = Arrays.asList(date1, date2);

        Collections.sort(dateList);

        assertThat(date2).isEqualTo(dateList.get(0));
    }

    // When same type with only one override but having different dates, override has higher priority
    @Test
    public void testCompareToOneOverrideSameTypesDiffDates() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, RELEASE_DATE_NOW, false);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, EARLIER_RELEASE_DATE, true);

        assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY);

        final var dateList = Arrays.asList(date1, date2);

        Collections.sort(dateList);

        assertThat(date2).isEqualTo(dateList.get(0));
    }

    // When same type with only one override but having same dates, override has higher priority
    @Test
    public void testCompareToOneOverrideSameTypesSameDates() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, true);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY);

        final var dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated and different types with different dates, later release date has higher priority
    @Test
    public void testCompareToNoOverridesDiffTypesDiffDates() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, LATER_RELEASE_DATE, false);

        assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY);

        final var dateList = Arrays.asList(date1, date2);

        Collections.sort(dateList);

        assertThat(date2).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesARDvsCRD() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because ARD higher priority than CRD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because CRD lower priority than ARD

        final var dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesARDvsNPD() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because ARD higher priority than NPD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because NPD lower priority than ARD

        final var dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesARDvsPRRD() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.PRRD, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because ARD higher priority than PRRD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because PRRD lower priority than ARD

        final var dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesCRDvsNPD() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, RELEASE_DATE_NOW, false);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because CRD higher priority than NPD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because NPD lower priority than CRD

        final var dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesCRDvsPRRD() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, RELEASE_DATE_NOW, false);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.PRRD, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because CRD higher priority than PRRD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because PRRD lower priority than CRD

        final var dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
    @Test
    public void testCompareToNoOverridesNPDvsPRRD() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, false);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.PRRD, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY);  // Because NPD higher priority than PRRD
        assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY); // Because PRRD lower priority than NPD

        final var dateList = Arrays.asList(date2, date1);

        Collections.sort(dateList);

        assertThat(date1).isEqualTo(dateList.get(0));
    }

    @Test
    public void testCompareToIdenticalObjects() {
        final var date1 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false);
        final var date2 = new NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false);

        assertThat(date1.compareTo(date2)).isEqualTo(SAME_PRIORITY);
        assertThat(date2.compareTo(date1)).isEqualTo(SAME_PRIORITY);
    }
}
