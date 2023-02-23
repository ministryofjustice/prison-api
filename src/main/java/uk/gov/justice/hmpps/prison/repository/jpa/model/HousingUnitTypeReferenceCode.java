package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(HousingUnitTypeReferenceCode.DOMAIN)
@NoArgsConstructor
public class HousingUnitTypeReferenceCode extends ReferenceCode {
    static final String DOMAIN = "HOU_UN_TYPE";

    public HousingUnitTypeReferenceCode(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
