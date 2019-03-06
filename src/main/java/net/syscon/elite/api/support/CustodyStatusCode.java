package net.syscon.elite.api.support;

/**
* Represents an offenders custody status code.
*/
public enum CustodyStatusCode {
    ACTIVE_OUT_CRT("Active-Out (CRT)"),
    ACTIVE_OUT_TAP("Active-Out (TAP)"),
    ACTIVE_IN("Active-In"),
    IN_TRANSIT("In-Transit"),
    ACTIVE_UAL("Active (UAL)"),
    ACTIVE_UAL_ECL("Active (UAL_ECL)"),
    ACTIVE_ESCP("Active (ESCP)"),
    IN_ACTIVE("In-Active"),
    IN_ACTIVE_OUT("Inactive-Out"),
    OTHER("Other");

    private String custodyStatusCode;

    CustodyStatusCode(final String custodyStatusCode) {
        this.custodyStatusCode = custodyStatusCode;
    }

    @Override
    public String toString(){
        return custodyStatusCode;
    }
}
