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
    public static final ReferenceCode.Pk HSHOSP = new Pk(AGY_LOC_TYPE, "HSHOSP");
    public static final ReferenceCode.Pk HOSPITAL = new Pk(AGY_LOC_TYPE, "HOSPITAL");

    public static final AgencyLocationType COURT_TYPE = new AgencyLocationType(CRT.getCode(), "Court");
    public static final AgencyLocationType PRISON_TYPE = new AgencyLocationType(INST.getCode(), "Prison");
    public static final AgencyLocationType HS_HOSPITAL_TYPE = new AgencyLocationType(HSHOSP.getCode(), "Secure Hospital");
    public static final AgencyLocationType HOSPITAL_TYPE = new AgencyLocationType(HSHOSP.getCode(), "Secure Hospital");

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

    public boolean isHospital() {
        return getCode().equals(HOSPITAL_TYPE.getCode()) || getCode().equals(HS_HOSPITAL_TYPE.getCode());
    }

}
