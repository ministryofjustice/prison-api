package uk.gov.justice.hmpps.prison.api.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class SentenceDetailTest {
    @Nested
    public class getHomeDetentionCurfewEndDate {
        @Test
        public void noConditionalReleaseDate() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .homeDetentionCurfewActualDate(LocalDate.parse("2010-01-02"))
                .build();
            assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isNull();
        }

        @Test
        public void noStartDate() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .conditionalReleaseDate(LocalDate.parse("2010-01-02"))
                .build();
            assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isNull();
        }

        @Test
        public void bothSet() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .homeDetentionCurfewActualDate(LocalDate.parse("2010-01-02"))
                .conditionalReleaseDate(LocalDate.parse("2019-02-04"))
                .build();
            assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isEqualTo("2019-02-03");
        }

        @Test
        public void overrideSetnoConditional() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .homeDetentionCurfewActualDate(LocalDate.parse("2010-01-02"))
                .conditionalReleaseOverrideDate(LocalDate.parse("2019-02-04"))
                .build();
            assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isEqualTo("2019-02-03");
        }

        @Test
        public void allSet() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .homeDetentionCurfewActualDate(LocalDate.parse("2010-01-02"))
                .conditionalReleaseDate(LocalDate.parse("2019-02-02"))
                .conditionalReleaseOverrideDate(LocalDate.parse("2019-02-04"))
                .build();
            assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isEqualTo("2019-02-03");
        }
    }

    @Nested
    public class getTopupSupervisionStartDate {
        @Test
        public void noConditionalReleaseDate() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isNull();
        }

        @Test
        public void noConditionalReleaseDateButLicenceExpiryDate() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
                .licenceExpiryDate(LocalDate.parse("2014-03-05"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2014-03-06");
        }

        @Test
        public void noExpiryDate() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .conditionalReleaseDate(LocalDate.parse("2010-01-02"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isNull();
        }

        @Test
        public void noLicenceExpiryDateConditionalReleaseDate() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
                .conditionalReleaseDate(LocalDate.parse("2019-02-04"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2019-02-04");
        }

        @Test
        public void noLicenceExpiryDateConditionalReleaseOverrideDateOnlySet() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
                .conditionalReleaseOverrideDate(LocalDate.parse("2019-02-04"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2019-02-04");
        }

        @Test
        public void noLicenceExpiryDate() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
                .conditionalReleaseDate(LocalDate.parse("2019-02-02"))
                .conditionalReleaseOverrideDate(LocalDate.parse("2019-02-04"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2019-02-04");
        }

        @Test
        public void licenceExpiryDate() {
            final var sentenceDetail = SentenceCalcDates.sentenceCalcDatesBuilder()
                .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
                .conditionalReleaseDate(LocalDate.parse("2019-02-04"))
                .licenceExpiryDate(LocalDate.parse("2014-03-05"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2014-03-06");
        }
    }
}
