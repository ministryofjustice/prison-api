package uk.gov.justice.hmpps.prison.api.support;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum SentenceAdjustmentType {
    RECALL_SENTENCE_REMAND("RSR"),
    TAGGED_BAIL("S240A"),
    RECALL_SENTENCE_TAGGED_BAIL("RST"),
    REMAND("RX"),
    UNUSED_REMAND("UR");

    private final String code;

    SentenceAdjustmentType(String code) {
        this.code = code;
    }

    public static SentenceAdjustmentType getByCode(String value) {
        return Stream.of(SentenceAdjustmentType.values())
            .filter(s -> s.code.equals(value))
            .findFirst()
            .orElse(null);
    }
}
