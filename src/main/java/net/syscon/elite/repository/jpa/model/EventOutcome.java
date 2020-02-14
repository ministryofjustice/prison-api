package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.OUTCOMES)
@NoArgsConstructor
public class EventOutcome extends ReferenceCode {
    public EventOutcome(final String code, final String description) {
        super(ReferenceCode.OUTCOMES, code, description);
    }
}
