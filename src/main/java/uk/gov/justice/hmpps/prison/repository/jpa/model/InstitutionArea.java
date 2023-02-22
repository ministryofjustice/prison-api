package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(InstitutionArea.INSTITUTION_AREA_DOMAIN)
@NoArgsConstructor
public class InstitutionArea extends ReferenceCode {

    static final String INSTITUTION_AREA_DOMAIN = "INST_AREA";

    public InstitutionArea(final String code, final String description) {
        super(INSTITUTION_AREA_DOMAIN, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(INSTITUTION_AREA_DOMAIN, code);
    }
}
