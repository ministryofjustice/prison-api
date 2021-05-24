package uk.gov.justice.hmpps.prison.api.model;

import lombok.Getter;

@Getter
public enum LegalStatus {
    RECALL("Recall"),
    DEAD("Dead"),
    INDETERMINATE_SENTENCE("Indeterminate Sentence"),
    SENTENCED("Sentenced"),
    CONVICTED_UNSENTENCED("Convicted Unsentenced"),
    CIVIL_PRISONER("Civil Prisoner"),
    IMMIGRATION_DETAINEE("Immigration Detainee"),
    REMAND("Remand"),
    UNKNOWN("Unknown"),
    OTHER("Other");

    private final String desc;

    LegalStatus(String desc) {
        this.desc = desc;
    }
}
