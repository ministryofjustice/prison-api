package uk.gov.justice.hmpps.prison.values;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum AccountCode {

    SPENDS("SPND", "spends"),
    SAVINGS("SAV", "savings"),
    CASH("REG", "cash");

    public final String code;
    public final String codeName;

    AccountCode(final String code, final String codeName) {
        this.code = code;
        this.codeName = codeName;
    }

    public static Optional<AccountCode> byCode(final String code) {
        return Arrays.stream(AccountCode.values()).filter(v -> v.code.equals(code.toUpperCase())).findFirst();
    }

    public static Optional<AccountCode> byCodeName(final String codeName) {
        return Arrays.stream(AccountCode.values()).filter(v -> v.codeName.equals(codeName.toLowerCase())).findFirst();
    }

    public static String codeForNameOrEmpty(final String codeName) {
        return byCodeName(codeName).map(code -> code.code).orElse("");
    }

    public static boolean exists(String codeName) {
       return !StringUtils.isEmpty(codeForNameOrEmpty(codeName));
    }
}
