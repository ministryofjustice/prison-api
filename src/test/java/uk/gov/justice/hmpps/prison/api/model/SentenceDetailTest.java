package uk.gov.justice.hmpps.prison.api.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class SentenceDetailTest {
    @Nested
    public class getHomeDetentionCurfewEndDate {
        @Test
        public void noReleaseDate() {
            final var sentenceDetail = SentenceDetail.sentenceDetailBuilder()
                .homeDetentionCurfewActualDate(LocalDate.parse("2010-01-02"))
                .build();
            assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isNull();
        }

        @Test
        public void noStartDate() {
            final var sentenceDetail = SentenceDetail.sentenceDetailBuilder()
                .releaseDate(LocalDate.parse("2010-01-02"))
                .build();
            assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isNull();
        }

        @Test
        public void bothSet() {
            final var sentenceDetail = SentenceDetail.sentenceDetailBuilder()
                .homeDetentionCurfewActualDate(LocalDate.parse("2010-01-02"))
                .releaseDate(LocalDate.parse("2019-02-04"))
                .build();
            assertThat(sentenceDetail.getHomeDetentionCurfewEndDate()).isEqualTo("2019-02-03");
        }
    }

    @Nested
    public class getTopupSupervisionStartDate {
        @Test
        public void noReleaseDate() {
            final var sentenceDetail = SentenceDetail.sentenceDetailBuilder()
                .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isNull();
        }

        @Test
        public void noExpiryDate() {
            final var sentenceDetail = SentenceDetail.sentenceDetailBuilder()
                .releaseDate(LocalDate.parse("2010-01-02"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isNull();
        }

        @Test
        public void noLicenceExpiryDate() {
            final var sentenceDetail = SentenceDetail.sentenceDetailBuilder()
                .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
                .releaseDate(LocalDate.parse("2019-02-04"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2019-02-04");
        }

        @Test
        public void licenceExpiryDate() {
            final var sentenceDetail = SentenceDetail.sentenceDetailBuilder()
                .topupSupervisionExpiryDate(LocalDate.parse("2010-01-02"))
                .releaseDate(LocalDate.parse("2019-02-04"))
                .licenceExpiryDate(LocalDate.parse("2014-03-05"))
                .build();
            assertThat(sentenceDetail.getTopupSupervisionStartDate()).isEqualTo("2014-03-06");
        }
    }
}
