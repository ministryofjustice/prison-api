package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ScheduledEventType.EVENT_TYPE)
@NoArgsConstructor
public class ScheduledEventType extends ReferenceCode {

    static final String EVENT_TYPE = "INT_SCH_RSN";

    public ScheduledEventType(final String code, final String description) {
        super(EVENT_TYPE, code, description);
    }
}

