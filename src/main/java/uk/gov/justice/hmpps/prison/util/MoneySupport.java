package uk.gov.justice.hmpps.prison.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneySupport {

    private static final Long PENCE_IN_POUND = 100L;

    public static Long poundsToPence(final BigDecimal amountInPounds) {
        return amountInPounds
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2).longValue();
    }

    public static BigDecimal penceToPounds(final Long penceValue) {
        return BigDecimal.valueOf(penceValue)
                .divide(BigDecimal.valueOf(PENCE_IN_POUND))
                .setScale(2, RoundingMode.HALF_UP);
    }
}