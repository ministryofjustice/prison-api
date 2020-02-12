package net.syscon.elite.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.MLTY_WZONE)
@NoArgsConstructor
public class WarZone extends ReferenceCode {
    public WarZone(final String code, final String description) {
        super(ReferenceCode.MLTY_WZONE, code, description);
    }
}
