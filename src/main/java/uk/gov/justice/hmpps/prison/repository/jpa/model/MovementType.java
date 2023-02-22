package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(MovementType.TYPE)
@NoArgsConstructor
public class MovementType extends ReferenceCode {

    public static final String TYPE = "MOVE_TYPE";

    public static final ReferenceCode.Pk REL = new ReferenceCode.Pk(TYPE, "REL");
    public static final ReferenceCode.Pk TRN = new ReferenceCode.Pk(TYPE, "TRN");
    public static final ReferenceCode.Pk ADM = new ReferenceCode.Pk(TYPE, "ADM");
    public static final ReferenceCode.Pk TAP = new ReferenceCode.Pk(TYPE, "TAP");
    public static final ReferenceCode.Pk CRT = new ReferenceCode.Pk(TYPE, "CRT");

    public MovementType(final String code, final String description) {
        super(TYPE, code, description);
    }

    public static MovementType of(ReferenceCode.Pk pk) {
        return new MovementType(pk.getCode(), pk.getDomain());
    }

    public static MovementType.Pk pk(final String code) {
        return new ReferenceCode.Pk(TYPE, code);
    }
}
