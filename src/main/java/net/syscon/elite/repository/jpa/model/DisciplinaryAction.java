package net.syscon.elite.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.MLTY_DISCP)
@NoArgsConstructor
public class DisciplinaryAction extends ReferenceCode {

    public static final ReferenceCode.Pk COURT_MARTIAL = new ReferenceCode.Pk(ReferenceCode.MLTY_DISCP, "CM");

    public DisciplinaryAction(final String code, final String description) {
        super(ReferenceCode.MLTY_DISCP, code, description);
    }
}
