package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(MovementReason.REASON)
@NoArgsConstructor
public class MovementReason extends ReferenceCode {

    public static final String REASON = "MOVE_RSN";

    public static final ReferenceCode.Pk COURT = new ReferenceCode.Pk(REASON, "CRT");
    public static final ReferenceCode.Pk TRANSFER_VIA_COURT = new ReferenceCode.Pk(REASON, "TRNCRT");
    public static final ReferenceCode.Pk TRANSFER_VIA_TAP = new ReferenceCode.Pk(REASON, "TRNTAP");
    public static final ReferenceCode.Pk DISCHARGE_TO_PSY_HOSPITAL = new ReferenceCode.Pk(REASON, "HP");
    public static final ReferenceCode.Pk AWAIT_REMOVAL_TO_PSY_HOSPITAL = new ReferenceCode.Pk(REASON, "O");
    public static final ReferenceCode.Pk CONDITIONAL_RELEASE = new ReferenceCode.Pk(REASON, "CR");

    public static final ReferenceCode.Pk SENTENCING = new ReferenceCode.Pk(REASON, "SENT");

    public MovementReason(final String code, final String description) {
        super(REASON, code, description);
    }

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(REASON, code);
    }

    public static MovementReason of(ReferenceCode.Pk pk) { return new MovementReason(pk.getCode(), pk.getDomain()); }
}
