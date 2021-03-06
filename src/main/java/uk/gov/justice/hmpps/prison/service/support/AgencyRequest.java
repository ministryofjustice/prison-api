package uk.gov.justice.hmpps.prison.service.support;

import org.apache.commons.lang3.Validate;

public abstract class AgencyRequest {
    private final String agencyId;

    protected AgencyRequest(final String agencyId) {
        Validate.notBlank(agencyId, "Agency id is required.");

        this.agencyId = agencyId;
    }

    public String getAgencyId() {
        return agencyId;
    }
}
