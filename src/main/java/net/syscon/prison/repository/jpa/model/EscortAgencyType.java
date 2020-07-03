package net.syscon.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(EscortAgencyType.ESCORT_CODE)
@NoArgsConstructor
public class EscortAgencyType extends ReferenceCode {

    static final String ESCORT_CODE = "ESCORT";

    public EscortAgencyType(final String code, final String description) {
        super(ESCORT_CODE, code, description);
    }

    public static ReferenceCode.Pk pk(final String key) {
        return new Pk(ESCORT_CODE, key);
    }
}
