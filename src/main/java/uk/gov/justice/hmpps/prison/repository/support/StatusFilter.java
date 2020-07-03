package uk.gov.justice.hmpps.prison.repository.support;

import lombok.Getter;

@Getter
public enum StatusFilter {
    ALL(null), ACTIVE_ONLY("Y"), INACTIVE_ONLY("N");

    private String activeFlag;

    StatusFilter(final String activeFlag) {
        this.activeFlag = activeFlag;
    }
}
