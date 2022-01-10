package uk.gov.justice.hmpps.prison.api.support;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum BookingAdjustmentType {
    SPECIAL_REMISSION("SREM"),
    ADDITIONAL_DAYS_AWARDED("ADA"),
    RESTORED_ADDITIONAL_DAYS_AWARDED("RADA"),
    UNLAWFULLY_AT_LARGE("UAL"),
    LAWFULLY_AT_LARGE("LAL");

    private final String code;

    BookingAdjustmentType(String code) {
        this.code = code;
    }

    public static BookingAdjustmentType getByCode(String value) {
        return Stream.of(BookingAdjustmentType.values())
            .filter(s -> s.code.equals(value))
            .findFirst()
            .orElse(null);
    }
}
