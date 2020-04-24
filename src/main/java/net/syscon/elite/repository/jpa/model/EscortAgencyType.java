package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(EscortAgencyType.ESCORT_CODE)
@NoArgsConstructor
public class EscortAgencyType extends ReferenceCode {

    static final String ESCORT_CODE = "ESCORT";

    public static final ReferenceCode.Pk PRISON_ESCORT_CUSTODY_SERVICES = new Pk(ESCORT_CODE, "PECS");

    public EscortAgencyType(final String code, final String description) {
        super(ESCORT_CODE, code, description);
    }
}
