package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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
    public static final AgencyLocationType HOSPITAL_TYPE = new AgencyLocationType(HOSPITAL.getCode(), "Hospital");

    public AgencyLocationType(final String code, final String description) {
        super(AGY_LOC_TYPE, code, description);
    }

    public AgencyLocationType(final String code) {
        super(AGY_LOC_TYPE, code, null);
    }

    public boolean isCourt() {
        return COURT_TYPE.getCode().equals(getCode());
    }

    public boolean isPrison() {
        return PRISON_TYPE.getCode().equals(getCode());
    }

    public boolean isHospital() {
        return HOSPITAL_TYPE.getCode().equals(getCode()) || HS_HOSPITAL_TYPE.getCode().equals(getCode());
    }

}
