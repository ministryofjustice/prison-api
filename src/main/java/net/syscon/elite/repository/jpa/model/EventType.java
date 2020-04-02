package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(EventType.EVENT_TYPE)
@NoArgsConstructor
public class EventType extends ReferenceCode {

    static final String EVENT_TYPE = "EVENT_TYPE";

    public static final ReferenceCode.Pk COURT = new ReferenceCode.Pk(EVENT_TYPE, "CRT");

    public EventType(final String code, final String description) {
        super(EVENT_TYPE, code, description);
    }
}
