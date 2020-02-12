package net.syscon.elite.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.MLTY_DSCHRG)
@NoArgsConstructor
public class MilitaryDischarge extends ReferenceCode {
    public MilitaryDischarge(final String code, final String description) {
        super(ReferenceCode.MLTY_DSCHRG, code, description);
    }
}
