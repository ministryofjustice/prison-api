package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(HealthProblemType.HEALTH)
@NoArgsConstructor
public class  HealthProblemType extends ReferenceCode {

    public static final String HEALTH = "HEALTH";

    public HealthProblemType(final String code, final String description) {
        super(HEALTH, code, description);
    }

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(HEALTH, code);
    }
}
