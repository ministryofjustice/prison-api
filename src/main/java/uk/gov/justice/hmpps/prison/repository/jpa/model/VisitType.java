package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(VisitType.VISIT_TYPE)
@NoArgsConstructor
public class VisitType extends ReferenceCode {

    static final String VISIT_TYPE = "VISIT_TYPE";

    public VisitType(final String code, final String description) {
        super(VISIT_TYPE, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(VISIT_TYPE, code);
    }
}
