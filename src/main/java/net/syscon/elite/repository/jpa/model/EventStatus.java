package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(EventStatus.EVENT_STS)
@NoArgsConstructor
public class EventStatus extends ReferenceCode {

    static final String EVENT_STS = "EVENT_STS";

    public static final String SCHEDULED = "SCH";

    public static final ReferenceCode.Pk CANCELLED = new Pk(EVENT_STS, "CANC");

    public static final ReferenceCode.Pk SCHEDULED_APPROVED = new Pk(EVENT_STS, SCHEDULED);

    public EventStatus(final String code, final String description) {
        super(EVENT_STS, code, description);
    }

    public boolean isScheduled() {
        return SCHEDULED.equals(this.getCode());
    }
}
