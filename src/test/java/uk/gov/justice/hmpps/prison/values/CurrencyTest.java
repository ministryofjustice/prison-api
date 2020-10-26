package uk.gov.justice.hmpps.prison.values;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CurrencyTest {

    @Test
    public void testGBP() {
        assertThat(Currency.GBP.code).isEqualTo("GBP");
        assertThat(Currency.GBP.name).isEqualTo("British Pound");
        assertThat(Currency.GBP.symbol).isEqualTo("£");
    }

    @Test
    public void testByCode_And_CodeIsGBP() {
        var currencyOpl = Currency.byCode("GBP");
        assertThat(currencyOpl.isPresent()).isTrue();

        Currency currency = currencyOpl.get();
        assertThat(currency.code).isEqualTo("GBP");
        assertThat(currency.name).isEqualTo("British Pound");
        assertThat(currency.symbol).isEqualTo("£");
    }

    @Test
    public void testByCode_And_CodeIsUnsupported() {
        var currencyOpl = Currency.byCode("USD");
        assertThat(currencyOpl.isPresent()).isFalse();
    }
}