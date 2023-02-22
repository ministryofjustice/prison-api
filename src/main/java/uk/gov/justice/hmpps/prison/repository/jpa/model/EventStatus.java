package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(EventStatus.EVENT_STS)
@NoArgsConstructor
public class EventStatus extends ReferenceCode {

    static final String EVENT_STS = "EVENT_STS";

    private static final String SCHEDULED = "SCH";

    public static final ReferenceCode.Pk CANCELLED = new Pk(EVENT_STS, "CANC");

    public static final ReferenceCode.Pk SCHEDULED_APPROVED = new Pk(EVENT_STS, SCHEDULED);

    public static final ReferenceCode.Pk COMPLETED = new Pk(EVENT_STS, "COMP");

    public EventStatus(final String code, final String description) {
        super(EVENT_STS, code, description);
    }

    public boolean isScheduled() {
        return SCHEDULED.equals(this.getCode());
    }
}
