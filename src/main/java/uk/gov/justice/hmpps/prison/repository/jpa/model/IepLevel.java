package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(IepLevel.IEP_LEVEL)
@NoArgsConstructor
public class IepLevel extends ReferenceCode {

    static final String IEP_LEVEL = "IEP_LEVEL";

    public IepLevel(final String code, final String description) {
        super(IEP_LEVEL, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(IEP_LEVEL, code);
    }
}
