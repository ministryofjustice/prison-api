package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(MovementReason.REASON)
@NoArgsConstructor
public class MovementReason extends ReferenceCode {

    static final String REASON = "MOVE_RSN";

    public static final ReferenceCode.Pk COURT = new ReferenceCode.Pk(REASON, "CRT");

    public MovementReason(final String code, final String description) {
        super(REASON, code, description);
    }

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(REASON, code);
    }
}
