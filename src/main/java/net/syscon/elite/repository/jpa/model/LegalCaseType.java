package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.LEG_CASE_TYP)
@NoArgsConstructor
public class LegalCaseType extends ReferenceCode {
    public LegalCaseType(final String code, final String description) {
        super(ReferenceCode.LEG_CASE_TYP, code, description);
    }
}
