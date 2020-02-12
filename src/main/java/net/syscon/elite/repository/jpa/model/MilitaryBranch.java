package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.MLTY_BRANCH)
@NoArgsConstructor
public class MilitaryBranch extends ReferenceCode {
    public MilitaryBranch(final String code, final String description) {
        super(ReferenceCode.MLTY_BRANCH, code, description);
    }
}
