package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class OffenderSentenceTest {

    @Test
    @DisplayName("isRecallable should return value from database field")
    void isRecallable_shouldReturnDatabaseValue() {
        var sentence = OffenderSentence.builder()
            .recallable(true)
            .build();
        
        assertThat(sentence.isRecallable()).isTrue();
        
        var nonRecallableSentence = OffenderSentence.builder()
            .recallable(false)
            .build();
        
        assertThat(nonRecallableSentence.isRecallable()).isFalse();
    }

    @Test
    @DisplayName("sentenceClassification should return value from database field")
    void sentenceClassification_shouldReturnDatabaseValue() {
        var sentence = OffenderSentence.builder()
            .sentenceClassification("STANDARD")
            .build();
        
        assertThat(sentence.getSentenceClassification()).isEqualTo("STANDARD");
        
        var extendedSentence = OffenderSentence.builder()
            .sentenceClassification("EXTENDED")
            .build();
        
        assertThat(extendedSentence.getSentenceClassification()).isEqualTo("EXTENDED");
    }

    @Test
    @DisplayName("getSentenceAndOffenceDetail should include database-backed recall fields")
    void getSentenceAndOffenceDetail_shouldIncludeRecallFields() {
        var sentCalcType = SentenceCalcType.builder()
            .calculationType("ADIMP_ORA")
            .description("ORA CJA03 Standard Determinate Sentence")
            .category("2003")
            .build();
        
        var sentence = OffenderSentence.builder()
            .id(new OffenderSentence.PK(12345L, 1))
            .calculationType(sentCalcType)
            .lineSequence(1L)
            .status("A")
            .recallable(true)
            .sentenceClassification("STANDARD")
            .build();
        
        var result = sentence.getSentenceAndOffenceDetail();
        
        assertThat(result.getIsRecallable()).isTrue();
        assertThat(result.getSentenceClassification()).isEqualTo("STANDARD");
        assertThat(result.getBookingId()).isEqualTo(12345L);
        assertThat(result.getSentenceSequence()).isEqualTo(1);
    }

    @Test
    @DisplayName("getSentenceAndOffenceDetail should handle null values")
    void getSentenceAndOffenceDetail_shouldHandleNullValues() {
        var sentCalcType = SentenceCalcType.builder()
            .calculationType("TEST")
            .description("Test Sentence")
            .category("TEST")
            .build();
        
        var sentence = OffenderSentence.builder()
            .id(new OffenderSentence.PK(12345L, 1))
            .calculationType(sentCalcType)
            .recallable(false)
            .sentenceClassification(null)
            .build();
        
        var result = sentence.getSentenceAndOffenceDetail();
        
        assertThat(result.getIsRecallable()).isFalse();
        assertThat(result.getSentenceClassification()).isNull();
    }

}