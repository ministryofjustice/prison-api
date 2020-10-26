package uk.gov.justice.hmpps.prison.values;

import java.util.Arrays;
import java.util.Optional;

public enum Currency {
    GBP("British Pound", "£", "GBP");
    public final String name;
    public final String symbol;
    public final String code;
    Currency(final String name, final String symbol, final String code) {
        this.name = name;
        this.symbol = symbol;
        this.code = code;
    }
    public static Optional<Currency> byCode(String code) {
        return Arrays.stream(values()).filter(v -> v.code.equals(code)).findFirst();
    }
}