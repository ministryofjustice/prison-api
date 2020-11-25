package uk.gov.justice.hmpps.prison.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneySupport {

    private static final Long PENCE_IN_POUND = 100L;

    public static Long poundsToPence(final BigDecimal amountInPounds) {
        return  toMoneyScale(amountInPounds).movePointRight(2).longValue();
    }

    public static BigDecimal penceToPounds(final Long penceValue) {
        return toMoneyScale(
            BigDecimal.valueOf(penceValue).divide(BigDecimal.valueOf(PENCE_IN_POUND))
        );
    }

    public static BigDecimal toMoneyScale(final BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal toMoney(final String amount) {
        return toMoneyScale(new BigDecimal(amount));
    }

    public static class MoneyDeserializer extends JsonDeserializer<BigDecimal> {
        private NumberDeserializers.BigDecimalDeserializer delegate = NumberDeserializers.BigDecimalDeserializer.instance;
        @Override
        public BigDecimal deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
            return toMoneyScale(delegate.deserialize(parser, context));
        }
    }
}