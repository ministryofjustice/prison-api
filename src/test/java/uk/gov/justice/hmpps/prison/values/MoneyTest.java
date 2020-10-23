package uk.gov.justice.hmpps.prison.values;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyTest {

    @Test
    public void testOnePoundIsCorrect() {
        assertThat(new Money(new BigDecimal(1L)).asText()).isEqualTo("£1.00");
        assertThat(new Money(new BigDecimal(1L)).getAmount()).isEqualTo(new BigDecimal("1.00").setScale(2, RoundingMode.HALF_UP));
        assertThat(new Money(new BigDecimal(1L)).getCurrency().symbol).isEqualTo("£");
        assertThat(new Money(new BigDecimal(1L)).getCurrency().name).isEqualTo("British Pound");
    }

    @Test
    public void testTwoPoundsIsCorrect() {
        assertThat(new Money(new BigDecimal(2L)).asText()).isEqualTo("£2.00");
    }

    @Test
    public void testTwoPoundsTwentyIsCorrect() {
        assertThat(new Money(new BigDecimal(2.20)).asText()).isEqualTo("£2.20");
    }

    @Test
    public void testTwoPoundsTwentyIsCorrectPence() {
        assertThat(new Money(new BigDecimal(2.20)).asPence()).isEqualTo(220);
    }

    @Test
    public void testTwoPoundsTwentyInPenceIsCorrectMoney() {
        assertThat(Money.asMoney(220L).asText()).isEqualTo("£2.20");
    }
}