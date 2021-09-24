package uk.gov.justice.hmpps.prison.repository.support;

import lombok.Getter;

@Getter
public enum StatusFilter {
    ALL(null), ACTIVE_ONLY(true), INACTIVE_ONLY(false);

    private final Boolean active;

    StatusFilter(final Boolean active) {
        this.active = active;
    }

    public String getActiveYesNo() {
       return active != null ? (active ? "Y" : "N") : null;
    }
}
