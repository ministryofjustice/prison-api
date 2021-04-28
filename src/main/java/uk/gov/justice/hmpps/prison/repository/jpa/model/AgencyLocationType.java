package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(AgencyLocationType.AGY_LOC_TYPE)
@NoArgsConstructor
public class AgencyLocationType extends ReferenceCode {
    public static final String AGY_LOC_TYPE = "AGY_LOC_TYPE";

    public static final ReferenceCode.Pk INST = new Pk(AGY_LOC_TYPE, "INST");
    public static final ReferenceCode.Pk CRT = new Pk(AGY_LOC_TYPE, "CRT");

    public static final AgencyLocationType COURT_TYPE = new AgencyLocationType("CRT", "Court");
    public static final AgencyLocationType PRISON_TYPE = new AgencyLocationType("INST", "Prison");

    public AgencyLocationType(final String code, final String description) {
        super(AGY_LOC_TYPE, code, description);
    }

    public AgencyLocationType(final String code) {
        super(AGY_LOC_TYPE, code, null);
    }

    public boolean isCourt() {
        return getCode().equals(COURT_TYPE.getCode());
    }

    public boolean isPrison() {
        return getCode().equals(PRISON_TYPE.getCode());
    }

}
