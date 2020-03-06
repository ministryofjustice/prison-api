package net.syscon.elite.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(MilitaryDischarge.MLTY_DSCHRG)
@NoArgsConstructor
public class MilitaryDischarge extends ReferenceCode {

    static final String MLTY_DSCHRG = "MLTY_DSCHRG";

    public MilitaryDischarge(final String code, final String description) {
        super(MLTY_DSCHRG, code, description);
    }
}
