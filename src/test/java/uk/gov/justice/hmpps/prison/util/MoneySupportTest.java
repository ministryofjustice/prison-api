package uk.gov.justice.hmpps.prison.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneySupportTest {

    @Test
    public void When_AmountIs10_Then_PenceValueIs1000() {
        assertThat(MoneySupport.poundsToPence(new BigDecimal("10.00"))).isEqualTo(1000);
    }

    @Test
    public void When_AmountIs10_20_Then_PenceValueIs1020() {
        assertThat(MoneySupport.poundsToPence(new BigDecimal("10.20"))).isEqualTo(1020);
    }

    @Test
    public void When_AmountIs20pOnly_Then_PenceValueIs20() {
        assertThat(MoneySupport.poundsToPence(new BigDecimal("0.20"))).isEqualTo(20);
    }

    @Test
    public void When_PenceIs1000_Then_PoundValueIs10_00() {
        assertThat(MoneySupport.penceToPounds(1000L))
                .isEqualTo(new BigDecimal("10.00")
                        .setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    public void When_PenceIs1020_Then_PoundValueIs10_20() {
        assertThat(MoneySupport.penceToPounds(1020L))
                .isEqualTo(new BigDecimal("10.20")
                        .setScale(2, RoundingMode.HALF_UP));
    }

}