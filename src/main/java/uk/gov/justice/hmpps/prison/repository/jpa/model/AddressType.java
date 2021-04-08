package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(AddressType.ADDR_TYPE)
@NoArgsConstructor
public class AddressType extends ReferenceCode {

    static final String ADDR_TYPE = "ADDR_TYPE";

    public AddressType(final String code, final String description) {
        super(ADDR_TYPE, code, description);
    }

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(ADDR_TYPE, code);
    }
}
