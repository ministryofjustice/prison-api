package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(LanguageReferenceCode.DOMAIN)
@NoArgsConstructor
public class LanguageReferenceCode extends ReferenceCode {
    static final String DOMAIN = "LANG";

    public LanguageReferenceCode(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
