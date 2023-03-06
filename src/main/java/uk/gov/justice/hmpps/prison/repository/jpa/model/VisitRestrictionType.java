package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(VisitRestrictionType.VISIT_RESTRICTION_TYPE)
@NoArgsConstructor
public class VisitRestrictionType extends ReferenceCode {

    static final String VISIT_RESTRICTION_TYPE = "VST_RST_TYPE";

    public VisitRestrictionType(final String code, final String description) {
        super(VISIT_RESTRICTION_TYPE, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(VISIT_RESTRICTION_TYPE, code);
    }
}
