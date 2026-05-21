@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SentenceDetailTest {
  @Nested
  inner class getHomeDetentionCurfewEndDate {
    @Test
    fun noConditionalReleaseDate() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .homeDetentionCurfewActualDate(LocalDate.parse("2010-01-02"))
        .build()
      assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isNull()
    }

    @Test
    fun noStartDate() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .conditionalReleaseDate(LocalDate.parse("2010-01-02"))
        .build()
      assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isNull()
    }

    @Test
    fun bothSet() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .homeDetentionCurfewActualDate(LocalDate.parse("2010-01-02"))
        .conditionalReleaseDate(LocalDate.parse("2019-02-04"))
        .build()
      assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isEqualTo("2019-02-03")
    }

    @Test
    fun overrideSetnoConditional() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .homeDetentionCurfewActualDate(LocalDate.parse("2010-01-02"))
        .conditionalReleaseOverrideDate(LocalDate.parse("2019-02-04"))
        .build()
      assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isEqualTo("2019-02-03")
    }

    @Test
    fun allSet() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .homeDetentionCurfewActualDate(LocalDate.parse("2010-01-02"))
        .conditionalReleaseDate(LocalDate.parse("2019-02-02"))
        .conditionalReleaseOverrideDate(LocalDate.parse("2019-02-04"))
        .build()
      assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isEqualTo("2019-02-03")
    }
  }

  @Nested
  inner class getTopupSupervisionStartDate {
    @Test
    fun noConditionalReleaseDate() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
        .build()
      assertThat(sentenceDetail.getTopupSupervisionStartDate()).isNull()
    }

    @Test
    fun noConditionalReleaseDateButLicenceExpiryDate() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
        .licenceExpiryDate(LocalDate.parse("2014-03-05"))
        .build()
      assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2014-03-06")
    }

    @Test
    fun noExpiryDate() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .conditionalReleaseDate(LocalDate.parse("2010-01-02"))
        .build()
      assertThat(sentenceDetail.getTopupSupervisionStartDate()).isNull()
    }

    @Test
    fun noLicenceExpiryDateConditionalReleaseDate() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
        .conditionalReleaseDate(LocalDate.parse("2019-02-04"))
        .build()
      assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2019-02-04")
    }

    @Test
    fun noLicenceExpiryDateConditionalReleaseOverrideDateOnlySet() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
        .conditionalReleaseOverrideDate(LocalDate.parse("2019-02-04"))
        .build()
      assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2019-02-04")
    }

    @Test
    fun noLicenceExpiryDate() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
        .conditionalReleaseDate(LocalDate.parse("2019-02-02"))
        .conditionalReleaseOverrideDate(LocalDate.parse("2019-02-04"))
        .build()
      assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2019-02-04")
    }

    @Test
    fun licenceExpiryDate() {
      val sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
        .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
        .conditionalReleaseDate(LocalDate.parse("2019-02-04"))
        .licenceExpiryDate(LocalDate.parse("2014-03-05"))
        .build()
      assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2014-03-06")
    }
  }
}
