package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(AddressType.ADDRESS_TYPE)
@NoArgsConstructor
public class AddressType extends ReferenceCode {

    static final String ADDRESS_TYPE = "ADDRESS_TYPE";

    public AddressType(final String code, final String description) {
        super(ADDRESS_TYPE, code, description);
    }
}
