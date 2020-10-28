package uk.gov.justice.hmpps.prison.values;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountCodeTest {

    @Test
    public void accountCodeEnumHasCorrectCodeAndName() {
        assertThat(AccountCode.SAVINGS.code).isEqualTo("SAV");
        assertThat(AccountCode.SAVINGS.codeName).isEqualTo("savings");
        assertThat(AccountCode.SPENDS.code).isEqualTo("SPND");
        assertThat(AccountCode.SPENDS.codeName).isEqualTo("spends");
        assertThat(AccountCode.CASH.code).isEqualTo("REG");
        assertThat(AccountCode.CASH.codeName).isEqualTo("cash");
    }

    @Test
    public void byCodeNameReturnsCorrectCode() {
        assertThat(AccountCode.byCodeName("savings").map(code -> code.code).orElse("")).isEqualTo("SAV");
        assertThat(AccountCode.byCodeName("spends").map(code -> code.code).orElse("")).isEqualTo("SPND");
        assertThat(AccountCode.byCodeName("cash").map(code -> code.code).orElse("")).isEqualTo("REG");
        assertThat(AccountCode.byCodeName("unknown").map(code -> code.code).orElse("")).isEqualTo("");
    }

    @Test
    public void byCodeReturnsCorrectCodeName() {
        assertThat(AccountCode.byCode("SAV").map(code -> code.codeName).orElse("")).isEqualTo("savings");
        assertThat(AccountCode.byCode("SPND").map(code -> code.codeName).orElse("")).isEqualTo("spends");
        assertThat(AccountCode.byCode("REG").map(code -> code.codeName).orElse("")).isEqualTo("cash");
        assertThat(AccountCode.byCode("unknown").map(code -> code.codeName).orElse("")).isEqualTo("");
    }

    @Test
    public void codeForNameOrEmptyReturnsCode() {
        assertThat(AccountCode.codeForNameOrEmpty("cash")).isEqualTo("REG");
        assertThat(AccountCode.codeForNameOrEmpty("spends")).isEqualTo("SPND");
        assertThat(AccountCode.codeForNameOrEmpty("savings")).isEqualTo("SAV");
        assertThat(AccountCode.codeForNameOrEmpty("unknown")).isEqualTo("");
    }
}