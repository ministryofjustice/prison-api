package net.syscon.elite.service.curfews;

import java.util.Set;

class StatusTrackingCodes {
    private StatusTrackingCodes() {
    }

    static final String MAN_CK_FAIL = "MAN_CK_FAIL";
    private static final String INELIGIBLE = "INELIGIBLE";

    private static final String MAN_CK_PASS = "MAN_CK_PASS";
    private static final String ELIGIBLE = "ELIGIBLE";

    static final String REFUSED = "REFUSED";
    private static final String GRANTED = "GRANTED";

    static final Set<String> CHECKS_FAILED_CODES = Set.of(MAN_CK_FAIL, INELIGIBLE);
    static final Set<String> CHECKS_PASSED_CODES = Set.of(MAN_CK_PASS, ELIGIBLE);

    static final Set<String> MAN_CK_FAIL_CODE = Set.of(MAN_CK_FAIL);
    static final Set<String> REFUSED_CODE = Set.of(REFUSED);
    static final Set<String> GRANTED_CODE = Set.of(GRANTED);

    static final Set<String> REFUSED_REASON_CODES = Set.of(MAN_CK_FAIL, REFUSED);

    static final Set<String> CHECKS_PASSED_AND_GRANTED_CODES = Set.of(MAN_CK_PASS, ELIGIBLE, GRANTED);
    static final Set<String> CHECKS_PASSED_AND_REFUSED_CODES = Set.of(MAN_CK_PASS, ELIGIBLE, REFUSED);
}
