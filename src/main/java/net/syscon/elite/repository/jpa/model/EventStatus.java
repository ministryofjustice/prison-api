package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.EVENT_STS)
@NoArgsConstructor
public class EventStatus extends ReferenceCode {
    public EventStatus(final String code, final String description) {
        super(ReferenceCode.EVENT_STS, code, description);
    }
}
