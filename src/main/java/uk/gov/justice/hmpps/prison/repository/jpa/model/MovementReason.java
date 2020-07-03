package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(MovementReason.REASON)
@NoArgsConstructor
public class MovementReason extends ReferenceCode {

    static final String REASON = "MOVE_RSN";

    public MovementReason(final String code, final String description) {
        super(REASON, code, description);
    }
}
