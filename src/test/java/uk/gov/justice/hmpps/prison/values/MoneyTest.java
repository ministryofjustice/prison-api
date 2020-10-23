package uk.gov.justice.hmpps.prison.values;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyTest {

    @Test
    public void testOnePoundIsCorrect() {
        var  amount = new BigDecimal(1L);
        assertThat(new Money(amount).asText()).isEqualTo("£1.00");
        assertThat(new Money(amount).getAmount()).isEqualTo(new BigDecimal("1.00").setScale(2, RoundingMode.HALF_UP));
        assertThat(new Money(amount).getCurrency().symbol).isEqualTo("£");
        assertThat(new Money(amount).getCurrency().name).isEqualTo("British Pound");
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

    @Test
    public void test50PenceCorrect() {
        var amount = new BigDecimal(0.5);
        assertThat(new Money(amount).asText()).isEqualTo("£0.50");
        assertThat(new Money(amount).getAmount()).isEqualTo(new BigDecimal("0.50").setScale(2, RoundingMode.HALF_UP));
        assertThat(new Money(amount).getCurrency().symbol).isEqualTo("£");
        assertThat(new Money(amount).getCurrency().name).isEqualTo("British Pound");
    }
}