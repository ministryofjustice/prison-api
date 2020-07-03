package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(HousingAttributeReferenceCode.DOMAIN)
@NoArgsConstructor
public class HousingAttributeReferenceCode extends ReferenceCode {
    static final String DOMAIN = "HOU_UNIT_ATT";

    public HousingAttributeReferenceCode(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
