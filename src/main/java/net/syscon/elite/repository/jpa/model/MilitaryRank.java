package net.syscon.elite.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.MLTY_RANK)
@NoArgsConstructor
public class MilitaryRank extends ReferenceCode {
    public MilitaryRank(final String code, final String description) {
        super(ReferenceCode.MLTY_RANK, code, description);
    }
}
