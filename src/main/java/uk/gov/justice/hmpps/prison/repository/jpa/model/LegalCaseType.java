package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(LegalCaseType.LEG_CASE_TYP)
@NoArgsConstructor
public class LegalCaseType extends ReferenceCode {

    static final String LEG_CASE_TYP = "LEG_CASE_TYP";

    public LegalCaseType(final String code, final String description) {
        super(LEG_CASE_TYP, code, description);
    }
    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(LEG_CASE_TYP, code);
    }
}
