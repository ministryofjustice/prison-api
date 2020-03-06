package net.syscon.elite.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(WarZone.MLTY_WZONE)
@NoArgsConstructor
public class WarZone extends ReferenceCode {

    static final String MLTY_WZONE = "MLTY_WZONE";

    public WarZone(final String code, final String description) {
        super(MLTY_WZONE, code, description);
    }
}
