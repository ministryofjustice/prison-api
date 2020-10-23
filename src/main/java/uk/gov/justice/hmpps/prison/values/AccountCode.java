package uk.gov.justice.hmpps.prison.values;

import java.util.Arrays;
import java.util.Optional;

public enum AccountCode {

    SPENDS("SPND", "spends"),
    SAVINGS("SAV", "savings"),
    CASH("REG", "cash");

    public final String code;
    public final String codeName;

    AccountCode(String code, String codeName) {
        this.code = code;
        this.codeName = codeName;
    }

    public static Optional<AccountCode> byCode(String code) {
        return Arrays.stream(AccountCode.values()).filter(v -> v.code.equals(code.toUpperCase())).findFirst();
    }

    public static Optional<AccountCode> byCodeName(String codeName) {
        return Arrays.stream(AccountCode.values()).filter(v -> v.codeName.equals(codeName.toLowerCase())).findFirst();
    }
}
