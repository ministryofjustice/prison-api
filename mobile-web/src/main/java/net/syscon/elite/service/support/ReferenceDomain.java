package net.syscon.elite.service.support;

public enum ReferenceDomain {
    ALERT("ALERT"),
    CASE_NOTE_SOURCE("NOTE_SOURCE"),
    CASE_NOTE_TYPE("TASK_TYPE"),
    INTERNAL_SCHEDULE_REASON("INT_SCH_RSN"),
    INTERNAL_SCHEDULE_TYPE("INT_SCH_TYPE"),
    INTERNAL_LOCATION_USAGE("ILOC_USG");

    private final String domain;

    ReferenceDomain(String domainCode) {
        this.domain = domainCode;
    }

    public String getDomain() {
        return domain;
    }
}
