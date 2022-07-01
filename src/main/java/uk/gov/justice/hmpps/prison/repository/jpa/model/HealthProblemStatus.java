package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(HealthProblemStatus.HEALTH_STS)
@NoArgsConstructor
public class HealthProblemStatus extends ReferenceCode {
    static final String HEALTH_STS = "HEALTH_STS";

    public static Pk pk(final String code) {
        return new Pk(HEALTH_STS, code);
    }

    public HealthProblemStatus(final String code, final String description) {
        super(HEALTH_STS, code, description);
    }

}
