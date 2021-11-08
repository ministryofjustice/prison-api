package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(CalcReasonType.CALC_REASON_TYPE)
@NoArgsConstructor
public class CalcReasonType extends ReferenceCode {
    static final String CALC_REASON_TYPE = "CALC_REASON";

    public CalcReasonType(final String code, final String description) {
        super(CALC_REASON_TYPE, code, description);
    }
}
