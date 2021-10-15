package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(VisitOrderType.VISIT_ORDER_TYPE)
@NoArgsConstructor
public class VisitOrderType extends ReferenceCode {

    static final String VISIT_ORDER_TYPE = "VIS_ORD_TYPE";

    public VisitOrderType(final String code, final String description) {
        super(VISIT_ORDER_TYPE, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(VISIT_ORDER_TYPE, code);
    }
}
