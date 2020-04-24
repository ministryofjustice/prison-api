package net.syscon.elite.service.support;

public enum ReferenceDomain {
    ALERT("ALERT"),
    CASE_NOTE_SOURCE("NOTE_SOURCE"),
    CASE_NOTE_TYPE("TASK_TYPE"),
    INTERNAL_SCHEDULE_REASON("INT_SCH_RSN"),
    INTERNAL_SCHEDULE_TYPE("INT_SCH_TYPE"),
    INTERNAL_LOCATION_USAGE("ILOC_USG"),
    PERFORMANCE("PERFORMANCE"),
    EVENT_OUTCOME("PS_PA_OC"),
    ASSESSMENT_COMMITTEE_CODE("ASSESS_COMM"),
    CATEGORY("SUP_LVL_TYPE"),
    CELL_MOVE_REASON("CHG_HOUS_RSN");

    private final String domain;

    ReferenceDomain(final String domainCode) {
        this.domain = domainCode;
    }

    public String getDomain() {
        return domain;
    }
}
