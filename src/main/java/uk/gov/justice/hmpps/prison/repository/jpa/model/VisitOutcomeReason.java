package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(VisitOutcomeReason.VISIT_OUTCOME_REASON)
@NoArgsConstructor
public class VisitOutcomeReason extends ReferenceCode {

    static final String VISIT_OUTCOME_REASON = "MOVE_CANC_RS";

    public VisitOutcomeReason(final String code, final String description) {
        super(VISIT_OUTCOME_REASON, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(VISIT_OUTCOME_REASON, code);
    }
}
