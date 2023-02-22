package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(LivingUnitReasonReferenceCode.DOMAIN)
@NoArgsConstructor
public class LivingUnitReasonReferenceCode extends ReferenceCode {
    static final String DOMAIN = "LIV_UN_RSN";

    public LivingUnitReasonReferenceCode(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
