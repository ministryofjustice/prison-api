package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(EventOutcome.EVENT_OUTCOME)
@NoArgsConstructor
public class EventOutcome extends ReferenceCode {

    static final String EVENT_OUTCOME = "OUTCOMES";

    public EventOutcome(final String code, final String description) {
        super(EVENT_OUTCOME, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(EVENT_OUTCOME, code);
    }
}
