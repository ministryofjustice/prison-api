package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(HealthProblemCode.HEALTH_PBLM)
@NoArgsConstructor
public class HealthProblemCode extends ReferenceCode {
    static final String HEALTH_PBLM = "HEALTH_PBLM";

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(HEALTH_PBLM, code);
    }

    public HealthProblemCode(final String code, final String description) {
        super(HEALTH_PBLM, code, description);
    }

    public HealthProblemCode(final String code, final String description, final String parentDomain, final String parentCode) {
        super(HEALTH_PBLM, code, description, 99, parentDomain, parentCode, true);
    }
}
