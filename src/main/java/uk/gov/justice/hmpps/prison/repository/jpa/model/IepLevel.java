package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(IepLevel.IEP_LEVEL)
@NoArgsConstructor
public class IepLevel extends ReferenceCode {

    static final String IEP_LEVEL = "IEP_LEVEL";

    public IepLevel(final String code, final String description) {
        super(IEP_LEVEL, code, description);
    }
}
