package uk.gov.justice.hmpps.prison.values;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.google.common.base.Preconditions.checkNotNull;

public class Money {

    private static final Long PENCE_IN_POUND = 100L;

    public enum Currency {
        GBP("British Pound", "£");
        public final String name;
        public final String symbol;
        Currency(String name, String symbol) {
            this.name = name;
            this.symbol = symbol;
        }
    }

    private final Currency currency = Currency.GBP;
    private final BigDecimal amount;

    public Money(BigDecimal amount, Currency currency) {
        checkNotNull(amount, "amount can't be null");
        checkNotNull(currency, "currency can't be null");
        this.amount = amount;
    }

    public Money(BigDecimal amount) {
       this(amount, Currency.GBP);
    }

    public BigDecimal getAmount() {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Currency getCurrency() {
        return currency;
    }

    public Long asPence() {
        Long pounds = amount.longValue();
        Long pence = amount.subtract(BigDecimal.valueOf(pounds)).multiply(BigDecimal.valueOf(100L)).longValue();
        return (pounds * PENCE_IN_POUND) + pence;
    }

    public String asText() {
        return currency.symbol + getAmount().toString();
    }

    public static Money asMoney(Long penceValue) {
        BigDecimal amount =  BigDecimal.valueOf(penceValue).divide(BigDecimal.valueOf(PENCE_IN_POUND)).setScale(2);
        return build(amount);
    }

    public static Money build(BigDecimal amount) {
        return new Money(amount);
    }
}
