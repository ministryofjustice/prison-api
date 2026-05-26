package uk.gov.justice.hmpps.prison.service.support

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation
import java.time.LocalDate

/**
 * Created by andrewk on 21/09/2017.
 */
class NonDtoReleaseDateTest {
  @Test
  fun testConstructorReleaseDateTypeRequired() {
    assertThatThrownBy { NonDtoReleaseDate(null, LocalDate.now(), false) }
      .isInstanceOf(
        NullPointerException::class.java,
      )
  }

  @Test
  fun testConstructorReleaseDateRequired() {
    assertThatThrownBy {
      NonDtoReleaseDate(
        SentenceCalculation.NonDtoReleaseDateType.ARD,
        null,
        true,
      )
    }.isInstanceOf(
      NullPointerException::class.java,
    )
  }

  // When both are overrides but have different dates, later release date has higher priority
  @Test
  fun testCompareToBothOverridesDiffDates() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, true)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, EARLIER_RELEASE_DATE, true)

    assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY)
    assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY)

    val dateList = listOf(date2, date1).sorted()

    assertThat(date1).isEqualTo(dateList[0])
  }

  // When both are overrides but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
  @Test
  fun testCompareToBothOverridesSameDates() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, true)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, RELEASE_DATE_NOW, true)

    assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY) // Because CRD higher priority than NPD
    assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY) // Because NPD lower priority than CRD

    val dateList = listOf(date1, date2).sorted()

    assertThat(date2).isEqualTo(dateList[0])
  }

  // When different types with only one override but having different dates, later release date has higher priority
  @Test
  fun testCompareToOneOverrideDiffDates() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, true)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, LATER_RELEASE_DATE, false)

    assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY)
    assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY)

    val dateList = listOf(date1, date2).sorted()

    assertThat(date2).isEqualTo(dateList[0])
  }

  // When same type with only one override but having different dates, override has higher priority
  @Test
  fun testCompareToOneOverrideSameTypesDiffDates() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, RELEASE_DATE_NOW, false)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, EARLIER_RELEASE_DATE, true)

    assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY)
    assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY)

    val dateList = listOf(date1, date2).sorted()

    assertThat(date2).isEqualTo(dateList[0])
  }

  // When same type with only one override but having same dates, override has higher priority
  @Test
  fun testCompareToOneOverrideSameTypesSameDates() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, true)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, false)

    assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY)
    assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY)

    val dateList = listOf(date2, date1).sorted()

    assertThat(date1).isEqualTo(dateList[0])
  }

  // When both are calculated and different types with different dates, later release date has higher priority
  @Test
  fun testCompareToNoOverridesDiffTypesDiffDates() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, LATER_RELEASE_DATE, false)

    assertThat(date1.compareTo(date2)).isEqualTo(LOWER_PRIORITY)
    assertThat(date2.compareTo(date1)).isEqualTo(HIGHER_PRIORITY)

    val dateList = listOf(date1, date2).sorted()

    assertThat(date2).isEqualTo(dateList[0])
  }

  // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
  @Test
  fun testCompareToNoOverridesARDvsCRD() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, RELEASE_DATE_NOW, false)

    assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY) // Because ARD higher priority than CRD
    assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY) // Because CRD lower priority than ARD

    val dateList = listOf(date2, date1).sorted()

    assertThat(date1).isEqualTo(dateList[0])
  }

  // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
  @Test
  fun testCompareToNoOverridesARDvsNPD() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, false)

    assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY) // Because ARD higher priority than NPD
    assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY) // Because NPD lower priority than ARD

    val dateList = listOf(date2, date1).sorted()

    assertThat(date1).isEqualTo(dateList[0])
  }

  // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
  @Test
  fun testCompareToNoOverridesARDvsPRRD() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.PRRD, RELEASE_DATE_NOW, false)

    assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY) // Because ARD higher priority than PRRD
    assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY) // Because PRRD lower priority than ARD

    val dateList = listOf(date2, date1).sorted()

    assertThat(date1).isEqualTo(dateList[0])
  }

  // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
  @Test
  fun testCompareToNoOverridesCRDvsNPD() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, RELEASE_DATE_NOW, false)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, false)

    assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY) // Because CRD higher priority than NPD
    assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY) // Because NPD lower priority than CRD

    val dateList = listOf(date2, date1).sorted()

    assertThat(date1).isEqualTo(dateList[0])
  }

  // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
  @Test
  fun testCompareToNoOverridesCRDvsPRRD() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.CRD, RELEASE_DATE_NOW, false)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.PRRD, RELEASE_DATE_NOW, false)

    assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY) // Because CRD higher priority than PRRD
    assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY) // Because PRRD lower priority than CRD

    val dateList = listOf(date2, date1).sorted()

    assertThat(date1).isEqualTo(dateList[0])
  }

  // When both are calculated but have same date, priority determined by enumerated release date type (e.g. ARD > CRD > NPD > PRRD)
  @Test
  fun testCompareToNoOverridesNPDvsPRRD() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.NPD, RELEASE_DATE_NOW, false)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.PRRD, RELEASE_DATE_NOW, false)

    assertThat(date1.compareTo(date2)).isEqualTo(HIGHER_PRIORITY) // Because NPD higher priority than PRRD
    assertThat(date2.compareTo(date1)).isEqualTo(LOWER_PRIORITY) // Because PRRD lower priority than NPD

    val dateList = listOf(date2, date1).sorted()

    assertThat(date1).isEqualTo(dateList[0])
  }

  @Test
  fun testCompareToIdenticalObjects() {
    val date1 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false)
    val date2 = NonDtoReleaseDate(SentenceCalculation.NonDtoReleaseDateType.ARD, RELEASE_DATE_NOW, false)

    assertThat(date1.compareTo(date2)).isEqualTo(SAME_PRIORITY)
    assertThat(date2.compareTo(date1)).isEqualTo(SAME_PRIORITY)
  }

  companion object {
    private const val HIGHER_PRIORITY = -1
    private const val SAME_PRIORITY = 0
    private const val LOWER_PRIORITY = 1
    private val RELEASE_DATE_NOW: LocalDate = LocalDate.now()
    private val EARLIER_RELEASE_DATE: LocalDate = RELEASE_DATE_NOW.minusDays(5)
    private val LATER_RELEASE_DATE: LocalDate = RELEASE_DATE_NOW.plusDays(5)
  }
}
