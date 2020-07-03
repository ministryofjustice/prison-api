package net.syscon.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(LegalCaseType.LEG_CASE_TYP)
@NoArgsConstructor
public class LegalCaseType extends ReferenceCode {

    static final String LEG_CASE_TYP = "LEG_CASE_TYP";

    public LegalCaseType(final String code, final String description) {
        super(LEG_CASE_TYP, code, description);
    }
}
