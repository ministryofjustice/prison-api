package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(HousingUnitTypeReferenceCode.DOMAIN)
@NoArgsConstructor
public class HousingUnitTypeReferenceCode extends ReferenceCode {
    static final String DOMAIN = "HOU_UN_TYPE";

    public HousingUnitTypeReferenceCode(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
