package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.MOVE_CANC_RS)
@NoArgsConstructor
public class OutcomeReason extends ReferenceCode {
    public OutcomeReason(final String code, final String description) {
        super(ReferenceCode.MOVE_CANC_RS, code, description);
    }
}
