package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(LanguageReferenceCode.DOMAIN)
@NoArgsConstructor
public class LanguageReferenceCode extends ReferenceCode {
    static final String DOMAIN = "LANG";

    public LanguageReferenceCode(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
