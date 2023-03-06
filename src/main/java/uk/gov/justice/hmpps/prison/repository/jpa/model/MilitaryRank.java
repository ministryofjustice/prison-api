package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(MilitaryRank.MLTY_RANK)
@NoArgsConstructor
public class MilitaryRank extends ReferenceCode {

    static final String MLTY_RANK = "MLTY_RANK";

    public MilitaryRank(final String code, final String description) {
        super(MLTY_RANK, code, description);
    }
}
