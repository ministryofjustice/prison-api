package uk.gov.justice.hmpps.prison.api.support;

/**
 * Represents status of member of staff.
 */
public enum Status {
    ALL("ALL"),
    ACTIVE("ACTIVE"),
    INACTIVE("INACT");

    private final String statusName;

    Status(final String status) {
        this.statusName = status;
    }

    public String getSqlName() {
        return statusName;
    }

}
