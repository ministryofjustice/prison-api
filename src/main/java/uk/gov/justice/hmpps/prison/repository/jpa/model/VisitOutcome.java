package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(VisitOutcome.VISIT_OUTCOME)
@NoArgsConstructor
public class VisitOutcome extends ReferenceCode {

    static final String VISIT_OUTCOME = "VIS_COMPLETE";

    public VisitOutcome(final String code, final String description) {
        super(VISIT_OUTCOME, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(VISIT_OUTCOME, code);
    }
}
