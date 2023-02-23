package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(MilitaryBranch.MLTY_BRANCH)
@NoArgsConstructor
public class MilitaryBranch extends ReferenceCode {

    static final String MLTY_BRANCH = "MLTY_BRANCH";

    public static final ReferenceCode.Pk ARMY = new ReferenceCode.Pk(MLTY_BRANCH, "ARM");

    public MilitaryBranch(final String code, final String description) {
        super(MLTY_BRANCH, code, description);
    }
}
