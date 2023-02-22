package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(DisciplinaryAction.MLTY_DISCP)
@NoArgsConstructor
public class DisciplinaryAction extends ReferenceCode {

    static final String MLTY_DISCP = "MLTY_DISCP";

    public static final ReferenceCode.Pk COURT_MARTIAL = new ReferenceCode.Pk(MLTY_DISCP, "CM");

    public DisciplinaryAction(final String code, final String description) {
        super(MLTY_DISCP, code, description);
    }
}
